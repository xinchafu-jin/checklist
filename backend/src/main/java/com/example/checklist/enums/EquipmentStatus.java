package com.example.checklist.enums;

/**
 * 設備狀態。
 */
public enum EquipmentStatus {
    IN_SERVICE("使用中"),
    UNDER_REPAIR("待修"),
    RETIRED("報廢");

    private final String label;

    EquipmentStatus(String label) {
        this.label = label;
    }

    /** 顯示給使用者看的中文名稱。 */
    public String getLabel() {
        return label;
    }
}
