package com.example.checklist.dto.respones;

import java.util.List; // 儲存每一題的統計結果

/**
 * 整張問卷的統計結果。
 */
public class QuizStatResponse {

    private Long quizId;

    private String quizTitle;

    // 總填答人數
    private Long totalRespondents;

    private List<QuestionStatDto> questionStats;

    /** @return 問卷 id */
    public Long getQuizId() {
        return quizId;
    }

    /** @param quizId 要設定的問卷 id */
    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    /** @return 問卷標題 */
    public String getQuizTitle() {
        return quizTitle;
    }

    /** @param quizTitle 要設定的問卷標題 */
    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    /** @return 總填答人數 */
    public Long getTotalRespondents() {
        return totalRespondents;
    }

    /** @param totalRespondents 要設定的總填答人數 */
    public void setTotalRespondents(Long totalRespondents) {
        this.totalRespondents = totalRespondents;
    }

    /** @return 每一題的統計結果 */
    public List<QuestionStatDto> getQuestionStats() {
        return questionStats;
    }

    /** @param questionStats 要設定的題目統計清單 */
    public void setQuestionStats(List<QuestionStatDto> questionStats) {
        this.questionStats = questionStats;
    }
}
