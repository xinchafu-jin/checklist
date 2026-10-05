package com.example.checklist.entity;

import com.example.checklist.enums.ResultLevel;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * 逐項的檢點結果，一個項目一列；選了哪些選項見 {@link InspectionResultOption}。
 */
@Entity
@Table(name = "inspection_result", uniqueConstraints = {
        @UniqueConstraint(name = "uk_result_item", columnNames = {"record_id", "item_id"})})
public class InspectionResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "record_id", nullable = false)
    private Long recordId;
    @Column(name = "item_id", nullable = false)
    private Long itemId;
    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 20)
    private ResultLevel result; // 文字項目為 null
    @Column(name = "number_value", precision = 12, scale = 3)
    private BigDecimal numberValue; // 數值項目的量測值
    @Column(name = "note", length = 500)
    private String note; // ATTENTION / ABNORMAL 必填；文字項目的內容也存這裡

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public ResultLevel getResult() {
        return result;
    }

    public void setResult(ResultLevel result) {
        this.result = result;
    }

    public BigDecimal getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(BigDecimal numberValue) {
        this.numberValue = numberValue;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
