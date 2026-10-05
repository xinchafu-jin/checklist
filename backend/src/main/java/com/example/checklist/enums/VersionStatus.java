package com.example.checklist.enums;

/**
 * 範本版本的狀態。同一範本同時只能有一個 PUBLISHED，由 service 檢查。
 */
public enum VersionStatus {
    /** 還在編輯，現場看不到。 */
    DRAFT("草稿"),
    /** 現場檢點使用的版本。 */
    PUBLISHED("發布"),
    /** 被新版本取代或不再使用。 */
    RETIRED("停用");

    private final String label;

    VersionStatus(String label) {
        this.label = label;
    }

    /** 顯示給使用者看的中文名稱。 */
    public String getLabel() {
        return label;
    }
}
