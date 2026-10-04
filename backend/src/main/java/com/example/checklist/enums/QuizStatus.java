package com.example.checklist.enums;

/**
 * 問卷目前狀態的列舉（Enum）。
 *
 * <p>Enum 適合表示「只能從固定選項中選一個」的資料，
 * 可避免程式中出現任意字串或拼字錯誤。</p>
 */
public enum QuizStatus {
    /** 尚未發布的草稿。 */
    DRAFT("草稿"),

    /** 已發布，但開始日期尚未到。 */
    NOT_STARTED("未開始"),

    /** 已到開始日期，且尚未超過結束日期。 */
    IN_PROGRESS("進行中"),

    /** 已超過結束日期。 */
    ENDED("已結束");

    // final 代表每一個 Enum 建立後，它的中文標籤不能再被更換。
    private final String label;

    /**
     * Enum 的建構子。
     *
     * <p>建立 {@code DRAFT("草稿")} 時，括號內的「草稿」會傳入 label。
     * Enum 建構子不寫 public，外部只能使用上面已定義好的固定值。</p>
     *
     * @param label 顯示給使用者看的中文名稱
     */
    QuizStatus(String label) {
        this.label = label;
    }

    /**
     * 取得中文狀態名稱。
     *
     * @return 例如「草稿」或「進行中」
     */
    public String getLabel() {
        return label;
    }
}
