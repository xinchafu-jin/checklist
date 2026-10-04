package com.example.checklist.dto.request;

import com.example.checklist.constants.ValidationMsg; // 共用的 DTO 驗證失敗訊息
import com.example.checklist.enums.QuestionType; // 限制題型只能是 SINGLE、MULTI 或 TEXT
import jakarta.validation.Valid; // 讓驗證繼續往內檢查每個 OptionRequest
import jakarta.validation.constraints.NotBlank; // 驗證字串不可為 null、空字串或只有空白
import jakarta.validation.constraints.NotNull; // 驗證物件或數值不可為 null

import java.util.List; // 儲存同一題的多個選項

/**
 * 前端新增單一題目時使用的 Request DTO。
 */
public class QuestionRequest {

    // Integer 是物件型別，可能為 null，因此用 @NotNull 檢查是否有傳題號。
    @NotNull(message = ValidationMsg.QUESTION_NUM_REQUIRED)
    private Integer questionNum; // 問題編號 (如: 1, 2, 3)

    // @NotBlank 比 @NotNull 更嚴格，只有空白的標題也不允許。
    @NotBlank(message = ValidationMsg.QUESTION_TITLE_REQUIRED)
    private String title;

    /*
     * Spring Boot 預設使用 Jackson 解析 JSON。當前端傳送 "SINGLE"、"MULTI" 或 "TEXT" 等字串時，
     * Jackson 會自動尋找 QuestionType 中名稱完全一致的列舉值並進行反序列化。
     * 注意：QuestionType 中已定義不區分大小寫的自動反序列化方法。
     */
    @NotNull(message = ValidationMsg.QUESTION_TYPE_REQUIRED)
    private QuestionType type;

    // 前端省略 isRequired 時使用 true；若明確傳 null，欄位仍會變成 null。
    private Boolean isRequired = true;

    /*
     * @Valid 會逐一驗證 List 中的 OptionRequest，
     * 因此 OptionRequest 的 @NotBlank 才會一起生效。
     */
    @Valid
    private List<OptionRequest> options;

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

    /** @return 題型 Enum */
    public QuestionType getType() {
        return type;
    }

    /** @param type SINGLE、MULTI 或 TEXT */
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

    /** @return 此題的選項清單；簡答題可能沒有選項 */
    public List<OptionRequest> getOptions() {
        return options;
    }

    /** @param options 要設定的選項清單 */
    public void setOptions(List<OptionRequest> options) {
        this.options = options;
    }
}
