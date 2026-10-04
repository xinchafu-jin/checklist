package com.example.checklist.dto.request;

import com.example.checklist.constants.ValidationMsg; // 共用的 DTO 驗證失敗訊息
import jakarta.validation.Valid; // 讓驗證繼續往內檢查每個 QuestionAnswerRequest
import jakarta.validation.constraints.Email; // 驗證字串是否為合法的 email 格式
import jakarta.validation.constraints.NotBlank; // 驗證字串不可為 null、空字串或只有空白
import jakarta.validation.constraints.NotEmpty; // 驗證清單不可為 null 或空集合
import jakarta.validation.constraints.NotNull; // 驗證問卷 id 不可為 null

import java.util.List; // 儲存一份問卷的多筆作答

/**
 * 使用者填寫問卷時送入 Controller 的最外層 Request DTO。
 *
 * <p>JSON 會先由 Jackson 轉成此物件，再由 {@code @Valid} 執行欄位驗證。</p>
 */
public class FillQuizRequest {

    // 一定要知道填的是哪一份問卷，所以問卷 id 不可為 null。
    @NotNull(message = ValidationMsg.QUIZ_ID_REQUIRED)
    private Long quizId;

    // 填答者 email 必須有值，且格式需符合 email 規範。
    @NotBlank(message = ValidationMsg.EMAIL_REQUIRED)
    @Email(message = ValidationMsg.EMAIL_FORMAT_INVALID)
    private String userEmail;

    // 逐一驗證 answers 中每個 QuestionAnswerRequest，且清單不可為空。
    @Valid
    @NotEmpty(message = ValidationMsg.QUESTION_ANSWERS_REQUIRED)
    private List<QuestionAnswerRequest> answers;

    /** @return 要填寫的問卷 id */
    public Long getQuizId() {
        return quizId;
    }

    /** @param quizId 要設定的問卷 id */
    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    /** @return 填答者 email */
    public String getUserEmail() {
        return userEmail;
    }

    /** @param userEmail 要設定的填答者 email */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /** @return 這份問卷的所有作答 */
    public List<QuestionAnswerRequest> getAnswers() {
        return answers;
    }

    /** @param answers 要設定的作答清單 */
    public void setAnswers(List<QuestionAnswerRequest> answers) {
        this.answers = answers;
    }
}