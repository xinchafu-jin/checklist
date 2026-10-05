package com.example.checklist.enums;

/**
 * 檢點結果的等級。
 *
 * <p>資料庫存的是意義而不是記號：畫面再顯示成 ✓ △ ✗ —，因為各家工廠的記號不一樣。</p>
 *
 * <p>severity 用來比較嚴重程度：多選項目取勾選選項中最嚴重的，
 * 整張紀錄的 overall_result 取所有項目中最嚴重的。不適用最低，不會蓋過任何結果。</p>
 */
public enum ResultLevel {
    NOT_APPLICABLE("不適用", 0),
    NORMAL("正常", 1),
    /** 需特別注意或保養；必須填說明，並自動開異常單。 */
    ATTENTION("注意", 2),
    /** 異常須送修；必須填說明，並自動開異常單。 */
    ABNORMAL("異常", 3);

    private final String label;
    private final int severity;

    ResultLevel(String label, int severity) {
        this.label = label;
        this.severity = severity;
    }

    /** 顯示給使用者看的中文名稱。 */
    public String getLabel() {
        return label;
    }

    /** 嚴重程度，數字越大越嚴重。 */
    public int getSeverity() {
        return severity;
    }

    /** 是否要填說明並開異常單。 */
    public boolean needsFollowUp() {
        return this == ATTENTION || this == ABNORMAL;
    }

    /** 回傳兩者中較嚴重的那一個；任一方為 null 時回傳另一方。 */
    public static ResultLevel worse(ResultLevel a, ResultLevel b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.severity >= b.severity ? a : b;
    }
}
