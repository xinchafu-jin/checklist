package com.example.checklist.enums;

import com.example.checklist.constants.ValidationMsg; // 共用的錯誤訊息常數
import com.fasterxml.jackson.annotation.JsonCreator; // 指定 Jackson 反序列化 JSON 時要呼叫的建立方法

/**
 * 題目種類的列舉（Enum）。
 *
 * <p>前端可以傳 Enum 名稱，例如 {@code "SINGLE"}，也可以傳中文名稱，
 * 例如 {@code "單選題"}；{@link #fromString(String)} 會負責轉換。</p>
 */
public enum QuestionType {
    /** 單選題，只能選擇一個答案。 */
    SINGLE("單選題"),//

    /** 多選題，可以選擇多個答案。 */
    MULTI("多選題"),//

    /** 簡答題，由使用者輸入文字。 */
    TEXT("簡答題");

    // 儲存顯示給使用者看的中文題型名稱。
    private String type;

    /**
     * 建立 Enum 固定值時，把括號內的中文文字保存到 type。
     *
     * @param type 題型的中文名稱
     */
    private QuestionType(String type) {
        this.type = type;
    }

    /*
     * @JsonValue 控制「Java 物件轉 JSON」時要輸出哪個值。
     * 目前沒有啟用，所以預設會輸出 SINGLE、MULTI 或 TEXT；
     * 若啟用，則會輸出 getType() 回傳的「單選題」等中文字。
     */
    //@JsonValue

    /**
     * 取得題型的中文名稱。
     *
     * @return 單選題、多選題或簡答題
     */
    public String getType() {
        return type;
    }

    /**
     * 將前端 JSON 字串轉成 QuestionType。
     *
     * <p>{@code @JsonCreator} 是 Jackson 解析 JSON 時的觸發標記；
     * 當某個 DTO 欄位型別是 QuestionType，Jackson 會自動呼叫此方法。</p>
     *
     * <p>支援 Enum 名稱（SINGLE、single）與中文標籤（單選題）。</p>
     *
     * @param input 前端傳入的題型文字
     * @return 對應的 QuestionType；輸入 null 或空白時回傳 null
     * @throws IllegalArgumentException 找不到對應題型時拋出
     */
    @JsonCreator
    public static QuestionType fromString(String input) {
        // || 是「或」；input 是 null 或空白時直接回傳 null，交給 DTO 的 @NotNull 驗證。
        if (input == null || input.isBlank()) {
            return null;
        }

        // values() 會取得 QuestionType 中定義的全部 Enum 值。
        for (QuestionType qType : QuestionType.values()) {
            // name() 取得 SINGLE 等 Enum 名稱；equalsIgnoreCase() 比對時忽略大小寫。
            if (qType.name().equalsIgnoreCase(input)) {
                return qType;
            }

            // 若不是 Enum 名稱，再比對單選題、多選題、簡答題等中文名稱。
            if (qType.getType().equalsIgnoreCase(input)) {
                return qType;
            }
        }

        // 全部比對失敗，表示前端傳入了系統不支援的題型。
        throw new IllegalArgumentException(String.format(ValidationMsg.QUESTION_TYPE_INVALID, input));
    }
}
