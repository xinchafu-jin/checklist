package com.example.checklist.enums;

import com.example.checklist.constants.ValidationMsg;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 檢點項目的類型。
 *
 * <p>沿用問卷 {@link QuestionType} 的單選、多選、簡答，再加上數值。
 * 不直接在 QuestionType 加 NUMBER，是因為舊的問卷程式把「不是 TEXT」當成「一定有選項」，
 * 數值項目會被誤判。QuestionType 之後隨問卷程式一起刪除。</p>
 */
public enum ItemType {
    /** 單選：結果就是選到的選項代表的等級。 */
    SINGLE("單選"),
    /** 多選：結果取勾選選項中最嚴重的等級。 */
    MULTI("多選"),
    /** 文字：只記錄內容，不參與判斷。 */
    TEXT("文字"),
    /** 數值：依上下限判斷，超出算異常。 */
    NUMBER("數值");

    private final String label;

    ItemType(String label) {
        this.label = label;
    }

    /** 顯示給使用者看的中文名稱。 */
    public String getLabel() {
        return label;
    }

    /** 單選、多選才有選項。 */
    public boolean hasOptions() {
        return this == SINGLE || this == MULTI;
    }

    /**
     * 將前端 JSON 字串轉成 ItemType，支援 Enum 名稱（SINGLE、single）與中文名稱（單選）。
     *
     * @param input 前端傳入的類型文字
     * @return 對應的 ItemType；輸入 null 或空白時回傳 null，交給 DTO 的 @NotNull 驗證
     * @throws IllegalArgumentException 找不到對應類型時拋出
     */
    @JsonCreator
    public static ItemType fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        for (ItemType type : ItemType.values()) {
            if (type.name().equalsIgnoreCase(input) || type.label.equals(input)) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.format(ValidationMsg.ITEM_TYPE_INVALID, input));
    }
}
