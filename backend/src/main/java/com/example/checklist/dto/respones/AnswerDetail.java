package com.example.checklist.dto.respones;

import com.example.checklist.enums.QuestionType; // 題型列舉：單選、多選或簡答

import java.util.List; // 儲存該題的所有選項與勾選狀態

/**
 * 單一問題的作答結果。
 *
 * <p>選擇題會填 {@code options}（列出所有選項與是否勾選），{@code answerText} 為 null；
 * 簡答題則相反。</p>
 */
public class AnswerDetail {

    // 題目 ID
    private Long questionId;

    // 問卷內的題目編號
    private Integer questionNum;

    // 題目名稱或內容
    private String questionTitle;

    private QuestionType questionType;

    // 選擇題: 列出所有選項及該填答者是否有勾選
    private List<OptionDetail> options;

    // 簡答題: 填答者的文字回覆
    private String answerText;

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

    /** @return 選擇題的選項清單；簡答題為 null */
    public List<OptionDetail> getOptions() {
        return options;
    }

    /** @param options 要設定的選項清單 */
    public void setOptions(List<OptionDetail> options) {
        this.options = options;
    }

    /** @return 簡答題的文字回覆；選擇題為 null */
    public String getAnswerText() {
        return answerText;
    }

    /** @param answerText 要設定的文字回覆 */
    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }
}
