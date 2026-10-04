package com.example.checklist.dto.respones;

import com.example.checklist.enums.QuestionType; // 題型列舉：單選、多選或簡答

import java.util.List; // 儲存選項統計或簡答文字清單

/**
 * 單一問題統計結果。
 *
 * <p>選擇題會填 {@code optionStats}，{@code textAnswers} 為 null；
 * 簡答題則相反。</p>
 */
public class QuestionStatDto {

    private Long questionId;

    private Integer questionNum;

    private String questionTitle;

    private QuestionType questionType;

    // 選擇題統計結果 (單選 / 多選)
    private List<OptionStatDto> optionStats;

    // 簡答題所有填答文字清單
    private List<String> textAnswers;

    /** @return 題目 id */
    public Long getQuestionId() {
        return questionId;
    }

    /** @param questionId 要設定的題目 id */
    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    /** @return 題目編號 */
    public Integer getQuestionNum() {
        return questionNum;
    }

    /** @param questionNum 要設定的題目編號 */
    public void setQuestionNum(Integer questionNum) {
        this.questionNum = questionNum;
    }

    /** @return 題目文字 */
    public String getQuestionTitle() {
        return questionTitle;
    }

    /** @param questionTitle 要設定的題目文字 */
    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    /** @return 題型 */
    public QuestionType getQuestionType() {
        return questionType;
    }

    /** @param questionType SINGLE、MULTI 或 TEXT */
    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    /** @return 選擇題的選項統計；簡答題為 null */
    public List<OptionStatDto> getOptionStats() {
        return optionStats;
    }

    /** @param optionStats 要設定的選項統計 */
    public void setOptionStats(List<OptionStatDto> optionStats) {
        this.optionStats = optionStats;
    }

    /** @return 簡答題的所有文字回覆；選擇題為 null */
    public List<String> getTextAnswers() {
        return textAnswers;
    }

    /** @param textAnswers 要設定的文字回覆清單 */
    public void setTextAnswers(List<String> textAnswers) {
        this.textAnswers = textAnswers;
    }
}
