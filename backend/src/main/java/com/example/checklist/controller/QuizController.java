package com.example.checklist.controller;

import com.example.checklist.dto.request.*;
import com.example.checklist.dto.respones.*;
import com.example.checklist.repository.UserRepository;
import com.example.checklist.service.QuizService; // 呼叫問卷相關的業務邏輯
import jakarta.validation.Valid; // 啟動 DTO 上的 @NotBlank、@NotNull 與巢狀 @Valid 驗證
import org.springframework.beans.factory.annotation.Autowired; // 讓 Spring 自動注入 QuizService
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus; // 提供 CREATED、BAD_REQUEST 等 HTTP 狀態碼
import org.springframework.http.ResponseEntity; // 同時包裝 HTTP 狀態碼、標頭與回應內容
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List; // 表示可包含多筆資料的有順序集合

/**
 * 問卷 REST API 的入口。
 *
 * <p>前端送出的 HTTP 請求會先進入 Controller；Controller 負責接收參數、
 * 呼叫 Service，最後使用 {@link ResponseEntity} 組合 HTTP 回應。</p>
 */
@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    // @Autowired 會向 Spring 容器取得已建立好的 QuizService Bean。
    @Autowired
    private QuizService quizService;
    @Autowired
    private UserRepository userRepository;

    /**
     * 建立新問卷。
     *
     * <p>{@code @PostMapping("/create")} 代表只接收
     * {@code POST /api/quiz/create}。</p>
     *
     * <p>{@code @RequestBody} 使用 Jackson 把 JSON 轉為 QuizRequest；
     * {@code @Valid} 接著執行 QuizRequest 與巢狀題目、選項的驗證規則。</p>
     *
     * @param request 前端送來的問卷資料
     * @return HTTP 201 Created 與成功訊息
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createQuiz(@Valid @RequestBody QuizRequest request) {
        // Controller 不直接操作資料庫，而是把工作交給 Service。
        quizService.createQuiz(request);

        // ResponseEntity.status(...).body(...) 是 Builder 寫法：先設定狀態碼，再放入回應內容。
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quiz created successfully!!"));
    }

    /**
     * 更新既有問卷，包含問卷基本資料、題目及選項。
     *
     * <p>完整 API URL：{@code POST http://localhost:8080/api/quiz/update}</p>
     *
     * <p>前端必須在 JSON Request Body 中提供要更新的問卷 {@code id}。
     * {@code @RequestBody} 會把 JSON 轉成 QuizRequest，
     * {@code @Valid} 會接著執行問卷、題目及選項的驗證規則。</p>
     *
     * @param request 前端送來的完整問卷資料，必須包含 id
     * @return HTTP 200 OK 與更新成功訊息
     */
    @PostMapping("/update")
    public ResponseEntity<ApiResponse> updateQuiz(@Valid @RequestBody QuizRequest request) {
        // 將完整更新流程交給 Service，Controller 不直接操作資料庫。
        quizService.updateQuiz(request);

        return ResponseEntity.ok(ApiResponse.success("Quiz updated successfully!!"));
    }

    /**
     * 取得所有問卷的基本資料，不包含題目與選項。
     *
     * <p>完整網址：{@code GET /api/quiz/get-all}</p>
     *
     * @return HTTP 200 OK 與問卷 DTO 清單
     */
    @GetMapping("/get-all")
    public ResponseEntity<List<QuizResponseDto>> getAllQuizzes() {
        List<QuizResponseDto> quizzes = quizService.getAllQuizzesWithoutQuestions();

        // ResponseEntity.ok(...) 是回傳 HTTP 200 的簡寫。
        return ResponseEntity.ok(quizzes);
    }

    /**
     * 根據問卷 id 取得題目與選項。
     *
     * <p>完整網址範例：{@code GET /api/quiz/10/questions}</p>
     *
     * <p>{@code {id}} 是路徑變數，{@code @PathVariable("id")} 會把網址中的
     * {@code 10} 轉成方法參數 {@code Long id}。</p>
     *
     * @param id 問卷 id
     * @return HTTP 200 OK 與題目 DTO 清單
     */
    @GetMapping("/{id}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByQuizId(@PathVariable("id") Long id) {
        List<QuestionResponse> questions = quizService.getQuestionsByQuizId(id);
        return ResponseEntity.ok(questions);
    }

    /**
     * 5. 批次刪除問卷
     * POST /api/quiz/delete
     */
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse> deleteQuizzes(//
                                                     @Valid @RequestBody QuizDeleteRequest request) {
        quizService.deleteQuizzes(request.getQuizIds());
        return ResponseEntity.ok(ApiResponse.success("Quizzes deleted successfully!!"));
    }

    /**
     * 6. 填寫並提交問卷
     * POST /api/quiz/fill
     *
     * <p>{@code @RequestBody} 使用 Jackson 把 JSON 轉為 FillQuizRequest；
     * {@code @Valid} 接著執行 FillQuizRequest 與巢狀作答的驗證規則。</p>
     *
     * @param request 前端送來的填答資料
     * @return HTTP 201 Created 與提交成功訊息
     */
    @PostMapping("/fill")
    public ResponseEntity<ApiResponse> fillQuiz(@Valid @RequestBody FillQuizRequest request) {
        quizService.fillQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quiz submitted successfully!!"));
    }

    /**
     * 7. 透過問卷 id 取得該張問卷所有填答者的回答
     * GET /api/quiz/{id}/submissions
     *
     * @param id 問卷 id
     * @return HTTP 200 OK 與每位填答者的提交紀錄
     */
    @GetMapping("/{id}/submissions")
    public ResponseEntity<List<QuizSubmissionResponse>> getQuizSubmissions(//
                                                                           @PathVariable("id") Long id) {
        List<QuizSubmissionResponse> submissions = quizService.getQuizSubmissions(id);
        return ResponseEntity.ok(submissions);
    }

    /**
     * 8. 取得問卷統計結果
     * GET /api/quiz/{id}/statistics
     *
     * @param id 問卷 id
     * @return HTTP 200 OK 與整張問卷的統計結果
     */
    @GetMapping("/{id}/statistics")
    public ResponseEntity<QuizStatResponse> getQuizStatistics(@PathVariable("id") Long id) {
        QuizStatResponse statistics = quizService.getQuizStatistics(id);
        return ResponseEntity.ok(statistics);
    }

    /**
     *
     * 9,
     *
     */
    @Transactional
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        quizService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("註冊成功"));
    }
    @Transactional
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword (@Valid @RequestBody ResetPasswordRequest resetPasswordRequest){
        quizService.resetPassword(resetPasswordRequest);
        return ResponseEntity.ok(ApiResponse.success("密碼以重設"));
    }

    @Transactional(readOnly = true)
    @GetMapping("/answered")
    public ResponseEntity<List<Long>> getAnsweredQuizIds (@RequestParam("email") String email){
        return ResponseEntity.ok(quizService.getAnsweredQuizIds(email));

    }

}
