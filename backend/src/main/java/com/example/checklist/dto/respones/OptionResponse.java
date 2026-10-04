package com.example.checklist.dto.respones;

/**
 * 回傳給前端的選項 Response DTO。
 *
 * <p>Service 會把 QuestionOption Entity 的資料複製到此物件，
 * 再由 Jackson 自動轉成 JSON。</p>
 */
public class OptionResponse {

    // 資料庫中選項的主鍵。
    private Long id;

    // 選項代碼，例如 A、B、C。
    private String optionCode;

    // 選項顯示內容，例如「非常滿意」。
    private String optionText;

    /** @return 選項 id */
    public Long getId() {
        return id;
    }

    /** @param id 要設定的選項 id */
    public void setId(Long id) {
        this.id = id;
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
}
