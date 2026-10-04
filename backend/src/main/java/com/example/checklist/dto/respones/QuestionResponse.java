package com.example.checklist.dto.respones;

import com.example.checklist.enums.QuestionType; // 限制回傳題型為 SINGLE、MULTI 或 TEXT

import java.util.List; // 儲存此題底下的多個選項 DTO

/**
 * 回傳給前端的題目 Response DTO。
 *
 * <p>除了題目基本資料，也包含 {@code List<OptionResponse>}，
 * 因此前端一次就能取得題目及它的所有選項。</p>
 */
public class QuestionResponse {

    // 題目主鍵。
    private Long id;

    // 題目在問卷中的編號。
    private Integer questionNum;

    // 題目文字。
    private String title;

    // 題型列舉。
    private QuestionType type;

    // 此題是否必填。
    private Boolean isRequired;

    // 此題的選項；簡答題通常會是空集合。
    private List<OptionResponse> options;
    /** @return 題目 id */
    public Long getId() {
        return id;
    }

    /** @param id 要設定的題目 id */
    public void setId(Long id) {
        this.id = id;
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
    public String getTitle() {
        return title;
    }

    /** @param title 要設定的題目文字 */
    public void setTitle(String title) {
        this.title = title;
    }

    /** @return 題型 */
    public QuestionType getType() {
        return type;
    }

    /** @param type 要設定的題型 */
    public void setType(QuestionType type) {
        this.type = type;
    }

    /** @return 此題是否必填 */
    public Boolean getIsRequired() {
        return isRequired;
    }

    /** @param isRequired 是否必填 */
    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    /** @return 選項清單 */
    public List<OptionResponse> getOptions() {
        return options;
    }

    /** @param options 要設定的選項清單 */
    public void setOptions(List<OptionResponse> options) {
        this.options = options;
    }
}
