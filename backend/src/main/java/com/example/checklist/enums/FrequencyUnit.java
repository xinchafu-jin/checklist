package com.example.checklist.enums;

/**
 * 排程頻率的單位，搭配 frequency_interval 使用，例如 WEEK × 2 = 每兩週。
 */
public enum FrequencyUnit {
    SHIFT("班"),
    DAY("日"),
    WEEK("週"),
    MONTH("月"),
    YEAR("年");

    private final String label;

    FrequencyUnit(String label) {
        this.label = label;
    }

    /** 顯示給使用者看的中文名稱。 */
    public String getLabel() {
        return label;
    }
}
