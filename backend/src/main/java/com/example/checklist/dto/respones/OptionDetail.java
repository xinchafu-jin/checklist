package com.example.checklist.dto.respones;

/**
 * 選項與勾選狀態。
 *
 * <p>用於「查看某位填答者的作答紀錄」時，列出該題的每一個選項，
 * 並標示這位填答者有沒有勾選它。</p>
 */
public class OptionDetail {

    // 選項 ID
    private Long optionId;

    // 選項代碼 (如: "A", "B", "C")
    private String optionCode;

    // 選項文字內容 (如: "滿意", "不滿意")
    private String optionText;

    // 填答者是否有勾選此選項
    private Boolean isSelected;

    public OptionDetail() {
        super();
    }

    public OptionDetail(Long optionId, String optionCode, String optionText, Boolean isSelected) {
        super();
        this.optionId = optionId;
        this.optionCode = optionCode;
        this.optionText = optionText;
        this.isSelected = isSelected;
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

    /** @return 此填答者是否勾選了這個選項 */
    public Boolean getIsSelected() {
        return isSelected;
    }

    /** @param isSelected 是否被勾選 */
    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }
}
