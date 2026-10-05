package com.example.checklist.enums;

/**
 * 檢點範本的類型。
 */
public enum TemplateKind {
    /** 依排程定期執行。 */
    INSPECTION("定期檢點"),
    /** 高風險作業開工前的許可，預留給之後的模組。 */
    PERMIT("作業許可");

    private final String label;

    TemplateKind(String label) {
        this.label = label;
    }

    /** 顯示給使用者看的中文名稱。 */
    public String getLabel() {
        return label;
    }
}
