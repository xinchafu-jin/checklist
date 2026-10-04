package com.example.checklist.service;

import com.example.checklist.constants.ValidationMsg;
import com.example.checklist.dto.request.*;
import com.example.checklist.dto.respones.*;
import com.example.checklist.entity.*;
import com.example.checklist.enums.QuestionType; // 題型列舉：單選、多選或簡答
import com.example.checklist.enums.UserRole;
import com.example.checklist.repository.*;
import org.springframework.beans.factory.annotation.Autowired; // 讓 Spring 自動注入需要的物件
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service; // 將類別標記成 Service，交由 Spring 管理
import org.springframework.transaction.annotation.Transactional; // 控制資料庫交易與回滾
import org.springframework.util.CollectionUtils; // 安全判斷集合是否為 null 或空集合

import java.math.BigDecimal; // 精確計算百分比，避免浮點數誤差
import java.math.RoundingMode; // 指定百分比的捨入方式
import java.time.LocalDate; // 表示只有年月日的日期，這裡用來取得今天
import java.util.*;
import java.util.stream.Collectors; // 將 Stream 處理結果收集成 List

/**
 * 問卷功能的 Service（業務邏輯層）。
 *
 * <p>Controller 負責接收 HTTP 請求，Repository 負責存取資料庫，
 * Service 則放在兩者中間，處理日期驗證、題型驗證、儲存順序及 DTO 轉換。</p>
 */
@Service
public class QuizService {

    /*
     * @Autowired 是 Spring 的依賴注入。
     * 程式不需要自己 new Repository，Spring 啟動時會自動放入可使用的實作物件。
     */
    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    @Autowired
    private QuizResponseRepository quizResponseRepository;

    @Autowired
    private ResponseDetailRepository responseDetailRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder encoder;


    /**
     * 新增一份完整問卷。
     *
     * <p>完整 API URL：{@code POST http://localhost:8080/api/quiz/create}</p>
     * <p>前端呼叫此 API 時，請求標頭應使用
     * {@code Content-Type: application/json}，並將完整問卷資料放在 Request Body。</p>
     *
     * <p>執行順序：</p>
     * <ol>
     *     <li>檢查問卷日期是否合理。</li>
     *     <li>先新增問卷，取得資料庫產生的問卷 id。</li>
     *     <li>使用問卷 id 新增所有題目。</li>
     *     <li>再使用題目 id 新增各題選項。</li>
     * </ol>
     *
     * <p>{@code @Transactional} 表示此方法內的資料庫操作屬於同一筆交易。
     * {@code rollbackFor = Exception.class} 表示途中只要發生 Exception，
     * 前面已做的新增也會回滾，避免只存到一半。</p>
     *
     * @param request 前端送來並由 Controller 轉換完成的問卷資料
     */
    @Transactional(rollbackFor = Exception.class)
    public void createQuiz(QuizRequest request) {
        // 儲存前先執行自訂的日期規則驗證。
        validateQuizTime(request);
        String creatorEmail = request.getCreatorEmail();
        if (creatorEmail == null || creatorEmail.isBlank()) {
            throw new IllegalArgumentException(ValidationMsg.QUIZ_CREATOR_REQUIRED);
        }
        boolean creatorExists = userRepository.existsByEmail(creatorEmail);
        if (!creatorExists) {
            throw new IllegalArgumentException(ValidationMsg.QUIZ_CREATOR_NOT_FOUND);

        }

        // 先新增 quiz 主資料；題目需要使用這筆卷產生的 id。
        quizRepository.insertQuiz(request.getTitle(), request.getDescription(), request.getStartDate(), request.getEndDate(),
                request.getIsPublished(), request.getCreatorEmail());

        // MySQL 的 LAST_INSERT_ID() 會取得這次新增資料所產生的流水號。
        Long newQuizId = quizRepository.getLastInsertedId();

        // 把問卷 id 與前端傳來的題目清單交給輔助方法處理。
        saveQuestionsAndOptions(newQuizId, request.getQuestions());
    }

    /**
     * 取得所有問卷列表，但不查詢題目與選項。
     *
     * <p>完整 API URL：{@code GET http://localhost:8080/api/quiz/get-all}</p>
     *
     * <p>{@code readOnly = true} 表示這個交易只讀取、不修改資料，
     * 可讓 Spring 與 JPA 針對純查詢做適當最佳化。</p>
     *
     * <p>quiz 表只存製作者的 email，畫面要顯示的是姓名，
     * 所以會先把 user 表整批撈成一個「email → 姓名」的對照表，
     * 再用它替每份問卷補上 creatorName。</p>
     *
     * @return 每份問卷的基本資料
     */
    @Transactional(readOnly = true)
    public List<QuizResponseDto> getAllQuizzesWithoutQuestions() {
        // Repository 從 quiz 資料表查出所有問卷。
        List<Quiz> quizzes = quizRepository.findAllQuizzes();

        /*
         * 先把所有使用者查出來，做成 email 對應姓名的 Map。
         *
         * 這一步是為了避免在下面的迴圈裡逐筆查 user：
         * 43 份問卷就會查 43 次資料庫（這叫 N+1 查詢）。
         * 使用者只有幾個，一次查完放進 Map，後面用 get() 直接拿。
         */
        List<User> users = userRepository.findAll();
        Map<String, String> nameByEmail = new HashMap<>();
        for (User user : users) {
            nameByEmail.put(user.getEmail(), user.getName());
        }

        // 逐份問卷轉成回傳用的 DTO。
        List<QuizResponseDto> result = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            QuizResponseDto quizResponse = new QuizResponseDto();
            quizResponse.setId(quiz.getId());
            quizResponse.setTitle(quiz.getTitle());
            quizResponse.setDescription(quiz.getDescription());
            quizResponse.setStartDate(quiz.getStartDate());
            quizResponse.setEndDate(quiz.getEndDate());
            quizResponse.setIsPublished(quiz.getIsPublished());
            quizResponse.setCreatorEmail(quiz.getCreatorEmail());
            quizResponse.setCreatedAt(quiz.getCreatedAt());
            quizResponse.setUpdatedAt(quiz.getUpdatedAt());

            /*
             * Map.get() 查不到時回傳 null，不會拋例外。
             * 舊資料的 creator_email 可能是 null、使用者也可能已被刪除，
             * 這兩種情況都回空字串，前端就不必再判斷一次 null。
             */
            String creatorName = nameByEmail.get(quiz.getCreatorEmail());
            if (creatorName == null) {
                creatorName = "";
            }
            quizResponse.setCreatorName(creatorName);

            quizResponse.setQuestions(null); // 此功能不回傳問題與選項
            result.add(quizResponse);
        }

        return result;
    }

    /**
     * 根據問卷 id 取得該問卷的所有題目與選項。
     *
     * <p>URL 格式：{@code GET http://localhost:8080/api/quiz/{id}/questions}</p>
     * <p>可直接測試的範例：{@code GET http://localhost:8080/api/quiz/1/questions}</p>
     * <p>範例中的 {@code 1} 是問卷 id，使用時請替換成資料庫中實際存在的 id。</p>
     *
     * @param quizId 網址中指定的問卷 id
     * @return 題目清單，每一題內含自己的選項清單
     * @throws RuntimeException 查不到指定問卷時拋出
     */
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByQuizId(Long quizId) {
        /*
         * findQuizById() 回傳 Optional。
         * orElseThrow(...) 表示 Optional 沒有值時，建立並拋出例外。
         */
        quizRepository.findQuizById(quizId)
                .orElseThrow(() -> new RuntimeException(String.format(ValidationMsg.QUIZ_NOT_FOUND, quizId)));

        // 問卷存在後，再依 quiz_id 查出它的所有題目。
        List<Question> questions = questionRepository.findByQuizId(quizId);

        /*
         * this::convertToQuestionResponse 是「方法參考」語法，
         * 等同於 question -> convertToQuestionResponse(question)。
         */
        return questions.stream()
                .map(this::convertToQuestionResponse)
                .collect(Collectors.toList());
    }

    /**
     * 更新一份完整問卷，包含問卷本身、題目及選項。
     *
     * <p>完整 API URL：{@code POST http://localhost:8080/api/quiz/update}</p>
     * <p>更新時，Request Body 的 {@code id} 必須是資料庫中已存在的問卷 id。</p>
     *
     * <p>執行順序：</p>
     * <ol>
     *     <li>檢查日期與問卷 id。</li>
     *     <li>更新 quiz 資料表中的問卷基本資料。</li>
     *     <li>先刪除原本的選項，再刪除原本的題目。</li>
     *     <li>重新新增前端送來的全部題目與選項。</li>
     * </ol>
     *
     * <p>{@code @Transactional} 表示以上操作屬於同一筆交易。
     * 中途若發生 RuntimeException，已完成的更新與刪除會一起回滾，
     * 避免問卷只更新到一半。</p>
     *
     * @param request 前端送來的完整問卷資料，更新時必須包含 id
     * @throws RuntimeException id 無效或資料庫中找不到該問卷時拋出
     */
    @Transactional
    public void updateQuiz(QuizRequest request) {
        // 更新前沿用新增功能的日期規則驗證。
        validateQuizTime(request);

        // Long 可以是 null；null、0 或負數都不是有效的資料庫 id。
        Long id = request.getId();
        if (id == null || id <= 0) {
            throw new RuntimeException(String.format(ValidationMsg.QUIZ_NOT_FOUND, id));
        }

        // 更新 quiz 主資料；回傳值代表實際更新到幾筆資料。
        int updatedRows = quizRepository.updateQuiz(id, request.getTitle(), request.getDescription(), request.getStartDate(),
                request.getEndDate(), request.getIsPublished());

        // 回傳 0 代表資料庫中沒有這個 id。
        if (updatedRows == 0) {
            throw new RuntimeException(String.format(ValidationMsg.QUIZ_NOT_FOUND, id));
        }

        /*
         * 採用「全部刪除後重建」的更新方式。
         * 必須先刪除 question_option，再刪除 question，
         * 因為選項透過 question_id 依賴題目。
         */
        questionOptionRepository.deleteByQuizId(id);
        questionRepository.deleteByQuizId(id);

        // 使用同一個問卷 id，重新寫入前端送來的所有題目與選項。
        saveQuestionsAndOptions(id, request.getQuestions());
    }


    /**
     * 5. 批次刪除問卷（包含對應的問題與選項）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuizzes(List<Long> quizIds) {
        // 1. 刪除這些問卷對應的所有選項
        questionOptionRepository.deleteByQuizIds(quizIds);
        // 2. 刪除這些問卷對應的所有問題
        questionRepository.deleteByQuizIds(quizIds);
        // 3. 刪除問卷本體
        int deletedCount = quizRepository.deleteByQuizIds(quizIds);
        if (deletedCount == 0) {
            throw new RuntimeException(ValidationMsg.QUIZ_DELETE_NONE);
        }
    }

    /**
     * 填寫並提交問卷。
     *
     * <p>完整 API URL：{@code POST http://localhost:8080/api/quiz/fill}</p>
     *
     * <p>執行順序：</p>
     * <ol>
     *     <li>確認問卷存在且已發布。</li>
     *     <li>確認今天在問卷可填寫的期間內。</li>
     *     <li>依資料庫題目逐一驗證必填與選項合法性。</li>
     *     <li>寫入 quiz_response 主表並取得回覆 id。</li>
     *     <li>依作答內容寫入 response_detail 明細表。</li>
     * </ol>
     *
     * <p>{@code @Transactional(rollbackFor = Exception.class)} 表示以上操作同屬一筆交易，
     * 中途發生任何 Exception 都會整批回滾，避免只寫入主表或部分明細。</p>
     *
     * @param request 前端送來並由 Controller 驗證完成的填答資料
     * @throws RuntimeException         查不到指定問卷時拋出
     * @throws IllegalArgumentException 問卷未發布、不在填寫期間或答案不合法時拋出
     */
    @Transactional(rollbackFor = Exception.class)
    public void fillQuiz(FillQuizRequest request) {
        /*
         * 1. 檢查問卷是否存在與發布狀態。
         * orElseThrow 是 Optional 提供的方法：容器裡有資料就取出 Quiz，
         * 容器是空的(沒找到)就執行括號內程式碼並拋出例外。
         */
        Quiz quiz = quizRepository.findQuizById(request.getQuizId())
                .orElseThrow(() -> new RuntimeException(
                        String.format(ValidationMsg.QUIZ_NOT_FOUND, request.getQuizId())));

        /*
         * 安全的布林值比較：防止 NullPointerException 的寫法；
         * 傳入 true 回傳 true，傳入 false 回傳 false，傳入 null 也不會報錯而是回傳 false。
         */
        if (!Boolean.TRUE.equals(quiz.getIsPublished())) {
            throw new IllegalArgumentException(ValidationMsg.FILL_QUIZ_NOT_PUBLISHED);
        }

        // 2. 檢查問卷可填寫時間：今天不能早於開始日，也不能晚於結束日。
        LocalDate now = LocalDate.now();
        if (now.isBefore(quiz.getStartDate())) {
            throw new IllegalArgumentException(ValidationMsg.FILL_QUIZ_NOT_STARTED);
        }
        if (now.isAfter(quiz.getEndDate())) {
            throw new IllegalArgumentException(ValidationMsg.FILL_QUIZ_ENDED);
        }

        // 3. 以資料庫題目為基準，檢查必填欄位與答案合法性。
        List<Question> questions = questionRepository.findByQuizId(quiz.getId());
        validateFillAnswers(questions, request.getAnswers());

        // 4. 寫入 quiz_response 主表。
        // 註：若此 Email 在此 Quiz 已填過，會觸發 uk_quiz_user 唯一約束並拋出 Database Exception。
        quizResponseRepository.insertQuizResponse(request.getQuizId(), request.getUserEmail());
        Long responseId = quizResponseRepository.getLastInsertedId();

        // 5. 寫入 response_detail 明細表。
        saveResponseDetails(responseId, request.getAnswers());
    }


    /**
     * 7. 透過問卷 id 取得所有填答者的回答紀錄（含所有選項及勾選狀態）。
     *
     * <p>完整 API URL：{@code GET http://localhost:8080/api/quiz/{id}/submissions}</p>
     *
     * <p>與統計不同，這支是「逐筆」列出每位填答者答了什麼；
     * 選擇題會把該題的所有選項都列出來，用 {@code isSelected} 標示有沒有被勾選。</p>
     *
     * @param quizId 問卷 id
     * @return 每位填答者的完整提交紀錄
     * @throws RuntimeException 查不到指定問卷時拋出
     */
    @Transactional(readOnly = true)
    public List<QuizSubmissionResponse> getQuizSubmissions(Long quizId) {
        // 1. 驗證問卷是否存在
        quizRepository.findQuizById(quizId)
                .orElseThrow(() -> new RuntimeException(String.format(ValidationMsg.QUIZ_NOT_FOUND, quizId)));

        // 2. 取得該問卷的所有問題與對應選項(預先載入，防範效能問題)
        List<Question> questions = questionRepository.findByQuizId(quizId);

        // 建立每個 questionId 對應的 Option List Map
        Map<Long, List<QuestionOption>> questionOptionsMap = questions.stream()
                .collect(Collectors.toMap(
                        Question::getId,
                        q -> questionOptionRepository.findByQuestionId(q.getId())
                ));

        // 3. 取得該問卷的所有填答紀錄主表
        List<QuizResponse> quizResponses = quizResponseRepository.findByQuizId(quizId);

        // 4. 將該問卷的所有填答紀錄組合成 Response
        return quizResponses.stream().map(response -> {
            QuizSubmissionResponse subDto = new QuizSubmissionResponse();
            subDto.setResponseId(response.getId());
            subDto.setQuizId(quizId);
            subDto.setUserEmail(response.getUserEmail());
            subDto.setSubmittedAt(response.getSubmittedAt());

            // 查出該次提交的所有 detail
            List<ResponseDetail> details = responseDetailRepository.findByResponseId(response.getId());

            // 依 questionId 分組明細 (一題可能多選，對應多筆 ResponseDetail)
            Map<Long, List<ResponseDetail>> detailMap = details.stream()
                    .collect(Collectors.groupingBy(ResponseDetail::getQuestionId));

            // 針對問卷的每一題，產生作答結果 AnswerDetail
            List<AnswerDetail> answerDetails = questions.stream().map(q -> {
                AnswerDetail ansDetail = new AnswerDetail();
                ansDetail.setQuestionId(q.getId());
                ansDetail.setQuestionNum(q.getQuestionNum());
                ansDetail.setQuestionTitle(q.getTitle());
                ansDetail.setQuestionType(q.getType());

                List<ResponseDetail> userDetails = detailMap.getOrDefault(q.getId(), Collections.emptyList());

                // 選擇題
                if (q.getType() == QuestionType.SINGLE || q.getType() == QuestionType.MULTI) {
                    // 收集使用者勾選的 option_id 集合
                    Set<Long> selectedOptionIds = userDetails.stream()
                            .map(ResponseDetail::getOptionId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    // 取得該題所有標準選項，並計算 isSelected
                    List<QuestionOption> options = questionOptionsMap.getOrDefault(q.getId(), Collections.emptyList());

                    List<OptionDetail> optionDtos = options.stream().map(opt ->
                            new OptionDetail(
                                    opt.getId(),
                                    opt.getOptionCode(),
                                    opt.getOptionText(),
                                    selectedOptionIds.contains(opt.getId()) // 是否被選取
                            )
                    ).collect(Collectors.toList());

                    ansDetail.setOptions(optionDtos);
                    ansDetail.setAnswerText(null);
                } else if (q.getType() == QuestionType.TEXT) {
                    // 文字題直接取得 answerText
                    String text = userDetails.stream()
                            .map(ResponseDetail::getAnswerText)
                            .filter(Objects::nonNull) // 過濾: 只保留 nonNull 的值
                            .findFirst() // 取得過濾後第一筆 nonNull 的資料
                            .orElse(null); // 如果過濾後沒有任何有效值，就回傳 null

                    ansDetail.setOptions(null);
                    ansDetail.setAnswerText(text);
                }
                return ansDetail;
            }).collect(Collectors.toList());

            subDto.setAnswers(answerDetails);
            return subDto;
        }).collect(Collectors.toList());
    }

    /**
     * 8. 取得問卷的統計分析結果。
     *
     * <p>完整 API URL：{@code GET http://localhost:8080/api/quiz/{id}/statistics}</p>
     *
     * <p>選擇題會算出每個選項的得票數與百分比（分母是總填答人數）；
     * 簡答題則把所有文字回覆整理成清單。</p>
     *
     * @param quizId 問卷 id
     * @return 整張問卷的統計結果
     * @throws RuntimeException 查不到指定問卷時拋出
     */
    @Transactional(readOnly = true)
    public QuizStatResponse getQuizStatistics(Long quizId) {
        // 1. 驗證問卷是否存在
        Quiz quiz = quizRepository.findQuizById(quizId)
                .orElseThrow(() -> new RuntimeException(String.format(ValidationMsg.QUIZ_NOT_FOUND, quizId)));

        // 2. 取得該問卷的所有填答紀錄主表 (計算總填答人數)
        List<QuizResponse> quizResponses = quizResponseRepository.findByQuizId(quizId);
        long totalRespondents = quizResponses.size();

        // 3. 取得該問卷的所有問題與選項
        List<Question> questions = questionRepository.findByQuizId(quizId);

        // 4. 收集所有填答細節
        // 先收集所有的 responseId
        List<Long> responseIds = quizResponses.stream()
                .map(QuizResponse::getId)
                .collect(Collectors.toList());

        // 如果完全無人填寫，直接回傳全為 0 的統計
        List<ResponseDetail> allDetails;
        if (responseIds.isEmpty()) {
            allDetails = Collections.emptyList();
        } else {
            // 查出該問卷所有填答者的回答明細
            allDetails = responseIds.stream()
                    .flatMap(resId -> responseDetailRepository.findByResponseId(resId).stream())
                    .collect(Collectors.toList());
        }

        // 5. 將明細依據 questionId 分組
        Map<Long, List<ResponseDetail>> detailsByQuestion = allDetails.stream()
                .collect(Collectors.groupingBy(ResponseDetail::getQuestionId));

        // 6. 計算每道題目的統計資料
        List<QuestionStatDto> questionStats = questions.stream().map(q -> {
            QuestionStatDto qStat = new QuestionStatDto();
            qStat.setQuestionId(q.getId());
            qStat.setQuestionNum(q.getQuestionNum());
            qStat.setQuestionTitle(q.getTitle());
            qStat.setQuestionType(q.getType());

            List<ResponseDetail> qDetails = detailsByQuestion.getOrDefault(q.getId(), Collections.emptyList());

            if (q.getType() == QuestionType.SINGLE || q.getType() == QuestionType.MULTI) {
                // 【選擇題】計算選項以及被選擇次數
                Map<Long, Long> optionCountMap = qDetails.stream()
                        .filter(d -> d.getOptionId() != null)
                        .collect(Collectors.groupingBy(ResponseDetail::getOptionId, Collectors.counting()));

                List<QuestionOption> options = questionOptionRepository.findByQuestionId(q.getId());

                List<OptionStatDto> optionStats = options.stream().map(opt -> {
                    // count: 該選項被勾選的總次數
                    long count = optionCountMap.getOrDefault(opt.getId(), 0L);

                    // 計算百分比 = (勾選人數 / 總填答人數) * 100
                    BigDecimal percentage = BigDecimal.ZERO;
                    if (totalRespondents > 0) {
                        percentage = BigDecimal.valueOf(count)
                                .multiply(BigDecimal.valueOf(100))
                                // BigDecimal.valueOf(totalRespondents): 除數(總填答人數)
                                // 2: 算出來的百分比會保留到「小數點後第 2 位」
                                // RoundingMode.HALF_UP: 代表捨入模式為四捨五入
                                .divide(BigDecimal.valueOf(totalRespondents), 2, RoundingMode.HALF_UP);
                    }

                    return new OptionStatDto(
                            opt.getId(),
                            opt.getOptionCode(),
                            opt.getOptionText(),
                            count,
                            percentage
                    );
                }).collect(Collectors.toList());

                qStat.setOptionStats(optionStats);
                qStat.setTextAnswers(null);
            } else if (q.getType() == QuestionType.TEXT) {
                // 【簡答題】收集所有文字回答 (過濾空白與 null)
                List<String> texts = qDetails.stream()
                        .map(ResponseDetail::getAnswerText)
                        .filter(t -> t != null && !t.isBlank())
                        .collect(Collectors.toList());

                qStat.setOptionStats(null);
                qStat.setTextAnswers(texts);
            }
            return qStat;
        }).collect(Collectors.toList());

        // 7. 組裝返回的 Response
        QuizStatResponse statResponse = new QuizStatResponse();
        statResponse.setQuizId(quiz.getId());
        statResponse.setQuizTitle(quiz.getTitle());
        statResponse.setTotalRespondents(totalRespondents);
        statResponse.setQuestionStats(questionStats);

        return statResponse;
    }

    // 註冊
    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException(ValidationMsg.USER_EMAIL_DUPLICATED);
        }
        if (userRepository.existsByPhone(registerRequest.getPhone())) {
            throw new IllegalArgumentException(ValidationMsg.USER_PHONE_DUPLICATED);
        }
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setName(registerRequest.getName());
        user.setAge(registerRequest.getAge());
        user.setPhone(registerRequest.getPhone());
        user.setRole(UserRole.USER);
        user.setPassword(encoder.encode(registerRequest.getPassword()));

        userRepository.save(user);
    }

    //登入
    public UserResponse login(LoginRequest loginRequest) {
        Optional<User> found = userRepository.findByEmail(loginRequest.getEmail());
        if (found.isEmpty()) {
            throw new IllegalArgumentException(ValidationMsg.LOGIN_FAILED);
        }
        User user = found.get();
        boolean ok = encoder.matches(loginRequest.getPassword(), user.getPassword());
        if (!ok) {
            throw new IllegalArgumentException(ValidationMsg.LOGIN_FAILED);
        }
        UserResponse userResponse = new UserResponse();
        userResponse.setEmail(user.getEmail());
        userResponse.setName(user.getName());
        userResponse.setRole(user.getRole());
        return userResponse;
    }

    //忘記密碼
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        Optional<User> found = userRepository.findByEmail(resetPasswordRequest.getEmail());
        if (found.isEmpty()) {
            throw new IllegalArgumentException(ValidationMsg.RESET_VERIFY_FAILED);
        }
        User user = found.get();
        if (!user.getPhone().equals(resetPasswordRequest.getPhone())) {
            throw new IllegalArgumentException(ValidationMsg.RESET_VERIFY_FAILED);
        }
        String newPassword = encoder.encode(resetPasswordRequest.getNewPassword());
        user.setPassword(newPassword);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<Long> getAnsweredQuizIds(String userEmail) {
       if(userEmail==null|| userEmail.isBlank()){
           throw new IllegalArgumentException(ValidationMsg.USER_EMAIL_REQUIRED);
       }
       return quizResponseRepository.findQuizIdsByUserEmail(userEmail);
    }


    // ==================== 私有輔助方法：只供 QuizService 內部使用 ====================

    /**
     * 依序儲存題目及其選項。
     *
     * @param quizId           題目所屬的問卷 id
     * @param questionRequests 前端傳來的題目清單；目前呼叫端必須確保它不是 null
     */
    private void saveQuestionsAndOptions(Long quizId, List<QuestionRequest> questionRequests) {
        // 增強型 for 迴圈：每次從 questionRequests 取出一個 QuestionRequest。
        for (QuestionRequest questionRequest : questionRequests) {
            // 單選題或多選題必須至少有一個選項。
            validateQuestionOptions(questionRequest);

            // Enum 的 name() 會把 QuestionType.SINGLE 轉成字串 "SINGLE" 存入資料庫。
            questionRepository.insertQuestion(
                    quizId,
                    questionRequest.getQuestionNum(),
                    questionRequest.getTitle(),
                    questionRequest.getType().name(),
                    questionRequest.getIsRequired());

            // 取得剛新增題目的流水號，後續選項要以它作為 question_id。
            Long newQuestionId = questionRepository.getLastInsertedId();

            /*
             * CollectionUtils.isEmpty(...) 在集合為 null 或沒有元素時都會回傳 true，
             * 前面的 ! 代表「不是空的」，此時才進入迴圈新增選項。
             */
            if (!CollectionUtils.isEmpty(questionRequest.getOptions())) {
                for (OptionRequest optionRequest : questionRequest.getOptions()) {
                    questionOptionRepository.insertOption(
                            newQuestionId,
                            optionRequest.getOptionCode(),
                            optionRequest.getOptionText());
                }
            }
        }
    }

    /**
     * 把資料庫 Entity 轉成要回傳給前端的 Response DTO。
     *
     * <p>不直接回傳 Entity，可以避免 API 格式與資料庫結構綁得太緊。</p>
     *
     * @param question 從資料庫查出的題目 Entity
     * @return 包含題目與選項的回應 DTO
     */
    private QuestionResponse convertToQuestionResponse(Question question) {
        QuestionResponse questionResponse = new QuestionResponse();
        questionResponse.setId(question.getId());
        questionResponse.setQuestionNum(question.getQuestionNum());
        questionResponse.setTitle(question.getTitle());
        questionResponse.setType(question.getType());
        questionResponse.setIsRequired(question.getIsRequired());

        // 使用題目 id 查詢該題的所有選項。
        List<QuestionOption> options = questionOptionRepository.findByQuestionId(question.getId());

        // Lambda 參數 option 代表 Stream 目前正在處理的單一 QuestionOption。
        List<OptionResponse> optionResponses = options.stream().map(option -> {
            OptionResponse optionResponse = new OptionResponse();
            optionResponse.setId(option.getId());
            optionResponse.setOptionCode(option.getOptionCode());
            optionResponse.setOptionText(option.getOptionText());
            return optionResponse;
        }).collect(Collectors.toList());
        questionResponse.setOptions(optionResponses);
        return questionResponse;
    }

    /**
     * 檢查問卷日期是否合法。
     *
     * <p>{@code isBefore()} 用來判斷呼叫它的日期是否早於括號內的日期。</p>
     *
     * <p>兩個條件分開檢查、分開拋出，是因為它們是兩種不同的錯：
     * 前者是日期填反了，後者是這份問卷已經開始。
     * 合成一個 {@code ||} 判斷的話只能共用一句訊息，
     * 使用者會拿著日期先後的提示，去找一個不在那裡的問題。</p>
     *
     * @param request 含開始日與結束日的問卷請求
     */
    private void validateQuizTime(QuizRequest request) {
        // QuizRequest 已經有檢查 startDate 和 endDate 不能是 null

        // 條件一：結束日期不能比開始日期早。
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException(ValidationMsg.QUIZ_END_BEFORE_START);
        }

        /*
         * 條件二：開始日期不能比今天早（最早只能是當天）。
         * 新增時代表填了過去的日期；
         * 更新時代表這份問卷已經開始，依規則不該再被編輯。
         */
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(ValidationMsg.QUIZ_ALREADY_STARTED);
        }
    }

    /**
     * 檢查選擇題是否有提供選項。
     *
     * <p>{@code != QuestionType.TEXT} 表示只要不是簡答題，就必須檢查選項清單。</p>
     *
     * @param questionRequest 要檢查的題目
     */
    private void validateQuestionOptions(QuestionRequest questionRequest) {
        // 若不是簡答題型(單、多選)，就必須要有選項 ==> 如果非簡答題 且 無選項  ==> 拋出例外
        if (questionRequest.getType() != QuestionType.TEXT) {
            if (questionRequest.getOptions() == null || questionRequest.getOptions().isEmpty()) {
                throw new IllegalArgumentException(String.format(
                        ValidationMsg.QUESTION_OPTIONS_REQUIRED, questionRequest.getQuestionNum()));
            }
        }
    }

    /**
     * 驗證使用者的填答是否合法。
     *
     * <p>以資料庫的題目為基準，逐題檢查必填規則與選項合法性。</p>
     *
     * @param questions 該問卷在資料庫中的所有題目
     * @param answers   使用者送出的作答清單
     * @throws IllegalArgumentException 必填未填、選擇了不屬於該題的選項或單選題選超過一個時拋出
     */
    private void validateFillAnswers(List<Question> questions, List<QuestionAnswerRequest> answers) {
        /*
         * 轉成 Map 的原因:
         * 1. 大幅提升執行效率(降低時間複雜度)
         * 2. 直接用 Key 查詢
         * 3. 自動過濾前端傳遞的「重複答案」:
         *    (k1, k2) -> k1 表示有重複 key 時，保留舊的 key(k1) 對應的值
         */
        Map<Long, QuestionAnswerRequest> answerMap = answers.stream()
                .collect(Collectors.toMap(QuestionAnswerRequest::getQuestionId, answer -> answer, (k1, k2) -> k1));

        // 以資料庫的標準題目為基準，逐一檢查使用者的答案是否合規
        for (Question question : questions) {
            QuestionAnswerRequest userAnswer = answerMap.get(question.getId());

            // 檢查必填題：必填題卻沒作答或作答為空，就拋出例外。
            if (Boolean.TRUE.equals(question.getIsRequired())) {
                if (userAnswer == null || isAnswerEmpty(question.getType(), userAnswer)) {
                    throw new IllegalArgumentException(String.format(
                            ValidationMsg.ANSWER_QUESTION_REQUIRED, question.getQuestionNum()));
                }
            }

            // 如果使用者有傳遞答案，且為選擇題，進行選項合法性檢查
            if (userAnswer != null
                    && (question.getType() == QuestionType.SINGLE || question.getType() == QuestionType.MULTI)) {
                List<Long> submittedOptionIds = userAnswer.getOptionIds();
                if (submittedOptionIds != null && !submittedOptionIds.isEmpty()) {
                    // 1. 自動去重（防止重複傳入相同的 option_id，例如 [10, 10, 11, 12, 13]）
                    Set<Long> uniqueSubmittedIds = new HashSet<>(submittedOptionIds);

                    // 2. 查出資料庫中該題目「真正擁有」的所有 Option IDs
                    List<QuestionOption> validOptions = questionOptionRepository.findByQuestionId(question.getId());
                    Set<Long> validOptionIds = validOptions.stream()
                            .map(QuestionOption::getId)
                            .collect(Collectors.toSet());

                    // 3. 數量與範圍驗證：
                    // A. 檢查傳入的選項數量是否超過資料庫該題的選項總數 (例如選項只有 4 個，答案卻有 5 個)
                    if (uniqueSubmittedIds.size() > validOptionIds.size()) {
                        throw new IllegalArgumentException(String.format(
                                ValidationMsg.ANSWER_TOO_MANY_OPTIONS, question.getQuestionNum()));
                    }

                    // B. 檢查傳入的每一個 option_id 是否真的屬於該題目 (防止偷渡其他題目的 option_id)
                    for (Long optionId : uniqueSubmittedIds) {
                        if (!validOptionIds.contains(optionId)) {
                            throw new IllegalArgumentException(String.format(
                                    ValidationMsg.ANSWER_OPTION_NOT_IN_QUESTION,
                                    question.getQuestionNum(), optionId));
                        }
                    }

                    // 4. 單選題規則檢查 (不可選超過 1 個不同選項)
                    if (question.getType() == QuestionType.SINGLE && uniqueSubmittedIds.size() > 1) {
                        throw new IllegalArgumentException(String.format(
                                ValidationMsg.ANSWER_SINGLE_CHOICE_ONLY, question.getQuestionNum()));
                    }
                }
            }
        }
    }

    /**
     * 根據題目類型（單選、多選或簡答），判斷使用者傳進來的答案內容是否為「空白／未填寫」。
     *
     * @param type   題目類型
     * @param answer 使用者對該題的作答
     * @return 內容為空回傳 true，有內容回傳 false
     */
    private boolean isAnswerEmpty(QuestionType type, QuestionAnswerRequest answer) {
        // 選擇題(單/多選): optionIds 沒有東西即視為空白
        if (type == QuestionType.SINGLE || type == QuestionType.MULTI) {
            return answer.getOptionIds() == null || answer.getOptionIds().isEmpty();
        } else if (type == QuestionType.TEXT) {
            // 簡答題: 沒有文字內容即視為空白
            return answer.getAnswerText() == null || answer.getAnswerText().isBlank();
        }
        return true;
    }

    /**
     * 將使用者的每一筆作答寫入 response_detail 明細表。
     *
     * <p>選擇題會為每個選中的 option_id 各寫入一筆；簡答題則寫入一筆文字內容。</p>
     *
     * @param responseId 對應的問卷回覆主檔 id
     * @param answers    使用者送出的作答清單
     */
    private void saveResponseDetails(Long responseId, List<QuestionAnswerRequest> answers) {
        for (QuestionAnswerRequest answer : answers) {
            if (answer.getOptionIds() != null && !answer.getOptionIds().isEmpty()) {
                // 選擇題：每個選項各存一筆，answer_text 留 null。
                for (Long optionId : answer.getOptionIds()) {
                    responseDetailRepository.insertDetail(responseId, answer.getQuestionId(), optionId, null);
                }
            } else if (answer.getAnswerText() != null && !answer.getAnswerText().isBlank()) {
                // 簡答題：存入文字內容，option_id 留 null。
                responseDetailRepository.insertDetail(responseId, answer.getQuestionId(), null, answer.getAnswerText());
            }
        }
    }


}
