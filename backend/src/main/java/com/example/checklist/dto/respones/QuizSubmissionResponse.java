package com.example.checklist.dto.respones;

import java.time.LocalDateTime; // 作答提交時間
import java.util.List; // 儲存該次提交的每一題作答結果

/**
 * 單一填答者的完整提交紀錄。
 *
 * <p>對應 {@code quiz_response} 一筆主檔，底下的 {@code answers}
 * 則是由 {@code response_detail} 整理而成。</p>
 */
public class QuizSubmissionResponse {

    private Long responseId;

    private Long quizId;

    private String userEmail;

    private LocalDateTime submittedAt;

    private List<AnswerDetail> answers;

    /** @return 作答主檔 id */
    public Long getResponseId() {
        return responseId;
    }

    /** @param responseId 要設定的作答主檔 id */
    public void setResponseId(Long responseId) {
        this.responseId = responseId;
    }

    /** @return 問卷 id */
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

    /** @return 提交時間 */
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    /** @param submittedAt 要設定的提交時間 */
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    /** @return 每一題的作答結果 */
    public List<AnswerDetail> getAnswers() {
        return answers;
    }

    /** @param answers 要設定的作答結果清單 */
    public void setAnswers(List<AnswerDetail> answers) {
        this.answers = answers;
    }
}
