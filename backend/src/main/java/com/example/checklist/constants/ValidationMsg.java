package com.example.checklist.constants;

/**
 * 集中管理所有 DTO 驗證失敗時要顯示的訊息。
 *
 * <p>把文字放在同一個類別，可以避免每個 DTO 重複寫相同字串，
 * 也能在需要修改訊息時只改一個地方。</p>
 *
 * <p>{@code public static final String} 的意思：</p>
 * <ul>
 *     <li>{@code public}：其他 package 也能使用。</li>
 *     <li>{@code static}：不需要先 {@code new ValidationMsg()} 就能使用。</li>
 *     <li>{@code final}：常數建立後不能再被修改。</li>
 * </ul>
 */
public class ValidationMsg {

    // ==================== QuizRequest 問卷欄位驗證訊息 ====================

    /** 問卷標題為 null、空字串或只有空白時使用。 */
    public static final String QUIZ_TITLE_REQUIRED = "Quiz title cannot be blank!!";

    /** 問卷開始日期沒有提供時使用。 */
    public static final String QUIZ_START_DATE_REQUIRED = "Start date cannot be null!!";

    /** 問卷結束日期沒有提供時使用。 */
    public static final String QUIZ_END_DATE_REQUIRED = "End date cannot be null!!";

    /** 問卷說明為 null、空字串或只有空白時使用。 */
    public static final String QUIZ_DESCRIPTION_REQUIRED = "Quiz description cannot be blank!!";

    /** 問卷是否發布沒有提供時使用。 */
    public static final String QUIZ_PUBLISHED_REQUIRED = "IsPublished status cannot be null!!";

    // ==================== QuestionRequest 題目欄位驗證訊息 ====================

    /** 題號沒有提供時使用。 */
    public static final String QUESTION_NUM_REQUIRED = "Question number cannot be null!!";

    /** 題目標題為 null、空字串或只有空白時使用。 */
    public static final String QUESTION_TITLE_REQUIRED = "Question title cannot be blank!!";

    /** 題型沒有提供時使用。 */
    public static final String QUESTION_TYPE_REQUIRED = "Question type cannot be null!!";

    // ==================== OptionRequest 選項欄位驗證訊息 ====================

    /** 選項代碼為 null、空字串或只有空白時使用。 */
    public static final String OPTION_CODE_REQUIRED = "Option code cannot be blank!!";

    /** 選項文字為 null、空字串或只有空白時使用。 */
    public static final String OPTION_TEXT_REQUIRED = "Option text cannot be blank!!";
    // ===================== QuizDeleteRequest 訊訊 ===============================
    public static final String QUIZ_IDS_REQUIRED = "Quiz IDs list cannot be empty!!";

    // ==================== FillQuizRequest 填寫問卷欄位驗證訊息 ====================

    /** 問卷 id 沒有提供時使用。 */
    public static final String QUIZ_ID_REQUIRED = "Quiz ID cannot be null!!";

    /** 填答者 email 為 null、空字串或只有空白時使用。 */
    public static final String EMAIL_REQUIRED = "Email address cannot be blank!!";

    /** email 格式不符合規範時使用。 */
    public static final String EMAIL_FORMAT_INVALID = "Invalid email format!!";

    /** 作答清單為空時使用。 */
    public static final String QUESTION_ANSWERS_REQUIRED = "Question answers list cannot be empty!!";

    /** 單筆作答缺少題目 id 時使用。 */
    public static final String QUESTION_ID_REQUIRED = "Question ID cannot be null!!";


    // ==================== User 欄位驗證訊息 ====================

    /** 使用者姓名為 null、空字串或只有空白時使用。 */
    public static final String USER_NAME_REQUIRED = "Name cannot be blank!!";

    /** 手機號碼為 null、空字串或只有空白時使用。 */
    public static final String USER_PHONE_REQUIRED = "Phone number cannot be blank!!";

    /** 使用者 email 為 null、空字串或只有空白時使用。 */
    public static final String USER_EMAIL_REQUIRED = "Email address cannot be blank!!";

    /** 密碼為 null、空字串或只有空白時使用。 */
    public static final String USER_PASSWORD_REQUIRED = "Password cannot be blank!!";

    /** 密碼長度不足時使用。 */
    public static final String USER_PASSWORD_TOO_SHORT = "Password must be at least 6 characters!!";

    // ==================== User 業務規則訊息 ====================

    /*
     * 上面那一區是「欄位格式不對」，由 DTO 上的 @NotBlank、@Size 等註解觸發；
     * 這一區是「格式沒問題，但不符合業務規則」，由 Service 主動 throw。
     * 兩者來源不同，訊息也不該共用。
     */

    /** 註冊時 email 已被其他帳號使用。 */
    public static final String USER_EMAIL_DUPLICATED = "This email is already registered!!";

    /** 註冊時手機號碼已被其他帳號使用。 */
    public static final String USER_PHONE_DUPLICATED = "This phone number is already registered!!";

    /*
     * 「查無此帳號」與「密碼錯誤」必須回同一句話。
     * 分開講的話，別人可以拿一批 email 來試，從訊息差異推測哪些已經註冊。
     */
    /** 登入失敗（查無帳號或密碼錯誤共用）。 */
    public static final String LOGIN_FAILED = "Incorrect account or password!!";

    /** 重設密碼時驗證失敗（查無 email 或手機不符共用，理由同上）。 */
    public static final String RESET_VERIFY_FAILED = "Email and phone number do not match!!";

    // ==================== Quiz 業務規則訊息 ====================

    /*
     * 這一區與下面兩區都是 Service 主動 throw 的訊息，
     * 與最上面「欄位驗證訊息」的來源不同（那些由 DTO 上的註解觸發）。
     *
     * 含有 %s、%d 的是訊息模板，使用時要搭配 String.format() 填入實際值，
     * 例如：String.format(ValidationMsg.QUIZ_NOT_FOUND, id)
     */

    /** 建立問卷時沒有帶製作者 email。 */
    public static final String QUIZ_CREATOR_REQUIRED = "Creator email cannot be blank!!";

    /** 建立問卷時帶的製作者 email 在 user 表中不存在。 */
    public static final String QUIZ_CREATOR_NOT_FOUND = "Creator account does not exist!!";

    /** 查不到指定 id 的問卷；%s 填入該 id（用 %s 是因為 id 可能是 null）。 */
    public static final String QUIZ_NOT_FOUND = "Quiz not found with id: %s";

    /** 批次刪除時一筆都沒刪到。 */
    public static final String QUIZ_DELETE_NONE = "No quizzes were found to delete!!";

    /** 結束日期早於開始日期。 */
    public static final String QUIZ_END_BEFORE_START = "End date cannot be earlier than start date!!";

    /*
     * 這句與上面那句原本共用同一個訊息，但它們是兩種完全不同的錯：
     * 上面是「日期填反了」，這裡是「這份問卷已經開始，不能再改動」。
     * 共用一句話的話，使用者會拿著日期先後的提示，去找一個根本不在那裡的問題。
     */
    /** 開始日期早於今天：新增時代表填了過去的日期，編輯時代表問卷已經開始。 */
    public static final String QUIZ_ALREADY_STARTED = "Start date cannot be earlier than today; a quiz that has already started cannot be edited!!";

    // ==================== 題目與選項業務規則訊息 ====================

    /** 選擇題沒有提供任何選項；%d 填入題號。 */
    public static final String QUESTION_OPTIONS_REQUIRED = "Question #%d requires at least one option!!";

    /** 前端傳入系統不支援的題型；%s 填入收到的字串。 */
    public static final String QUESTION_TYPE_INVALID = "Invalid question type: %s";

    // ==================== 登入 ====================

    public static final String EMPLOYEE_NO_REQUIRED = "請輸入工號。";

    public static final String LOGIN_PASSWORD_REQUIRED = "請輸入密碼。";

    /** 工號不存在、帳號停用、密碼錯誤都回這一句，別人才無法從登入頁試出哪些工號存在。 */
    public static final String AUTH_LOGIN_FAILED = "工號或密碼錯誤。";

    /** token 還有效，但帳號已被停用或刪除。 */
    public static final String AUTH_ACCOUNT_UNAVAILABLE = "帳號已停用或不存在，請重新登入。";

    /** 前端傳入系統不支援的檢點項目類型；%s 填入收到的字串。 */
    public static final String ITEM_TYPE_INVALID = "不支援的檢點項目類型：%s";

    // ==================== 填寫問卷業務規則訊息 ====================

    /** 問卷尚未發布，不能填答。 */
    public static final String FILL_QUIZ_NOT_PUBLISHED = "This quiz is not published yet!!";

    /** 問卷還沒到開始日期。 */
    public static final String FILL_QUIZ_NOT_STARTED = "This quiz has not started yet!!";

    /** 問卷已經超過結束日期。 */
    public static final String FILL_QUIZ_ENDED = "This quiz has already ended!!";

    /** 必填題沒有作答；%d 填入題號。 */
    public static final String ANSWER_QUESTION_REQUIRED = "Question #%d is required!!";

    /** 送來的選項數量超過該題實際擁有的選項總數；%d 填入題號。 */
    public static final String ANSWER_TOO_MANY_OPTIONS = "Question #%d has more selections than it has options!!";

    /** 送來的 option id 不屬於該題；第一個 %d 是題號，第二個是 option id。 */
    public static final String ANSWER_OPTION_NOT_IN_QUESTION = "Question #%d does not contain option id: %d";

    /** 單選題被選了超過一個選項；%d 填入題號。 */
    public static final String ANSWER_SINGLE_CHOICE_ONLY = "Question #%d is single-choice and allows only one option!!";

}
