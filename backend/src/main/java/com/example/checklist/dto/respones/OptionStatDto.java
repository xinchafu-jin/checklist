package com.example.checklist.dto.respones;

import java.math.BigDecimal; // 用來精確表示百分比，避免浮點數誤差

/**
 * 選項統計：得票數與百分比。
 */
public class OptionStatDto {

    private Long optionId;

    private String optionCode;

    private String optionText;

    // 選擇該選項的人數
    private Long selectedCount;

    // 得票百分比 (例如: 66.67)
    private BigDecimal percentage;

    public OptionStatDto() {
        super();
    }

    public OptionStatDto(Long optionId, String optionCode, String optionText, Long selectedCount,
            BigDecimal percentage) {
        super();
        this.optionId = optionId;
        this.optionCode = optionCode;
        this.optionText = optionText;
        this.selectedCount = selectedCount;
        this.percentage = percentage;
    }

    /** @return 選項 id */
    public Long getOptionId() {
        return optionId;
    }

    /** @param optionId 要設定的選項 id */
    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }

    /** @return 選項代碼 */
    public String getOptionCode() {
        return optionCode;
    }

    /** @param optionCode 要設定的選項代碼 */
    public void setOptionCode(String optionCode) {
        this.optionCode = optionCode;
    }

    /** @return 選項文字 */
    public String getOptionText() {
        return optionText;
    }

    /** @param optionText 要設定的選項文字 */
    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    /** @return 這個選項被幾個人勾選 */
    public Long getSelectedCount() {
        return selectedCount;
    }

    /** @param selectedCount 要設定的勾選人數 */
    public void setSelectedCount(Long selectedCount) {
        this.selectedCount = selectedCount;
    }

    /** @return 得票百分比 */
    public BigDecimal getPercentage() {
        return percentage;
    }

    /** @param percentage 要設定的得票百分比 */
    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
}
