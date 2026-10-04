package com.example.checklist.dto.request;

import com.example.checklist.constants.ValidationMsg; // 共用的 DTO 驗證失敗訊息
import jakarta.validation.constraints.NotNull; // 驗證題目 id 不可為 null

import java.util.List; // 儲存單選或多選題的多個選項 id

/**
 * 填寫問卷時，單一題目的作答內容。
 *
 * <p>依題型不同會使用不同欄位：</p>
 * <ul>
 *     <li>單選或多選題：使用 {@link #optionIds} 存放被選取的選項 id。</li>
 *     <li>簡答題：使用 {@link #answerText} 存放填答文字。</li>
 * </ul>
 */
public class QuestionAnswerRequest {

    // 每筆作答一定要對應到某個題目，所以題目 id 不可為 null。
    @NotNull(message = ValidationMsg.QUESTION_ID_REQUIRED)
    private Long questionId;

    // 單選或多選題對應的選項 id 列表。
    private List<Long> optionIds;

    // 簡答題內容。
    private String answerText;

    /** @return 作答對應的題目 id */
    public Long getQuestionId() {
        return questionId;
    }

    /** @param questionId 要設定的題目 id */
    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    /** @return 被選取的選項 id 列表 */
    public List<Long> getOptionIds() {
        return optionIds;
    }

    /** @param optionIds 要設定的選項 id 列表 */
    public void setOptionIds(List<Long> optionIds) {
        this.optionIds = optionIds;
    }

    /** @return 簡答題的填答文字 */
    public String getAnswerText() {
        return answerText;
    }

    /** @param answerText 要設定的簡答題文字 */
    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }
}