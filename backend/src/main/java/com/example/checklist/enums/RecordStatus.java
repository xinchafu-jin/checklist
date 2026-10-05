package com.example.checklist.enums;

/**
 * 檢點紀錄的狀態。
 */
public enum RecordStatus {
    COMPLETED("完成"),
    /** 機器當天沒開，留紀錄但不算漏檢。 */
    DOWNTIME("停機");

    private final String label;

    RecordStatus(String label) {
        this.label = label;
    }

    /** 顯示給使用者看的中文名稱。 */
    public String getLabel() {
        return label;
    }
}
