package com.example.checklist.enums;

/**
 * 異常單的狀態。
 */
public enum AbnormalityStatus {
    OPEN("待處理"),
    IN_PROGRESS("處理中"),
    CLOSED("已結案");

    private final String label;

    AbnormalityStatus(String label) {
        this.label = label;
    }

    /** 顯示給使用者看的中文名稱。 */
    public String getLabel() {
        return label;
    }
}
