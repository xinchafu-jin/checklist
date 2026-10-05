package com.example.checklist.entity;

import jakarta.persistence.*;

/**
 * 檢點結果選了哪些選項：單選一列、多選好幾列。
 */
@Entity
@Table(name = "inspection_result_option", uniqueConstraints = {
        @UniqueConstraint(name = "uk_result_option", columnNames = {"result_id", "option_id"})})
public class InspectionResultOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "result_id", nullable = false)
    private Long resultId;
    @Column(name = "option_id", nullable = false)
    private Long optionId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

    public Long getOptionId() {
        return optionId;
    }

    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }
}
