package com.example.checklist.dto.request;

import com.example.checklist.constants.ValidationMsg; // 共用的 DTO 驗證失敗訊息
import com.fasterxml.jackson.annotation.JsonAlias; // 讓同一欄位可以接受其他 JSON 名稱
import jakarta.validation.constraints.NotBlank; // 驗證字串不可為 null、空字串或只有空白

/**
 * 前端新增選項時使用的 Request DTO。
 *
 * <p>DTO（Data Transfer Object）只負責在前端與後端之間傳遞資料，
 * 不直接代表資料庫資料表。</p>
 */
public class OptionRequest {

    /*
     * 正式 JSON 欄位名稱是 optionCode。
     * @JsonAlias({"code"}) 讓前端改傳 code 時也能自動填入此欄位。
     */
//    @JsonAlias({"code"})
    @NotBlank(message = ValidationMsg.OPTION_CODE_REQUIRED)
    private String optionCode; // 選項編號 (如: A, B, C 或 數字 1, 2, 3 的字串)

    // optionText 或別名 optionName 都能被 Jackson 對應到這個欄位。
//    @JsonAlias({"optionName"})
    @NotBlank(message = ValidationMsg.OPTION_TEXT_REQUIRED)
    private String optionText; // 選項內容

    /**
     * 取得選項編號。
     *
     * @return 例如 A、B、C
     */
    public String getOptionCode() {
        return optionCode;
    }

    /**
     * 設定選項編號；Jackson 解析 JSON 時會呼叫 setter 填入資料。
     *
     * @param optionCode 選項編號
     */
    public void setOptionCode(String optionCode) {
        // this.optionCode 是物件欄位；右側 optionCode 是方法參數。
        this.optionCode = optionCode;
    }

    /**
     * 取得選項顯示文字。
     *
     * @return 選項內容
     */
    public String getOptionText() {
        return optionText;
    }

    /**
     * 設定選項顯示文字。
     *
     * @param optionText 選項內容
     */
    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }


}
