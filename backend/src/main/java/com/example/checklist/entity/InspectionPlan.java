package com.example.checklist.entity;

import com.example.checklist.enums.FrequencyUnit;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 檢點排程：哪台設備套用哪張範本、多久檢一次。頻率 = 單位 × 間隔；schedule_days 的格式見 V1__create_schema.sql。
 */
@Entity
@Table(name = "inspection_plan", uniqueConstraints = {
        @UniqueConstraint(name = "uk_plan_equipment_template", columnNames = {"equipment_id", "template_id"})})
public class InspectionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;
    @Column(name = "template_id", nullable = false)
    private Long templateId;
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_unit", nullable = false, length = 10)
    private FrequencyUnit frequencyUnit;
    @Column(name = "frequency_interval", nullable = false)
    private Integer frequencyInterval = 1; // 每幾個單位一次
    @Column(name = "schedule_days", length = 100)
    private String scheduleDays; // 例如 MON,TUE,WED,THU,FRI,SAT,SUN
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "active", nullable = false)
    private Boolean active = true;
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt; // 資料庫自動填入
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt; // 資料庫自動更新

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public FrequencyUnit getFrequencyUnit() {
        return frequencyUnit;
    }

    public void setFrequencyUnit(FrequencyUnit frequencyUnit) {
        this.frequencyUnit = frequencyUnit;
    }

    public Integer getFrequencyInterval() {
        return frequencyInterval;
    }

    public void setFrequencyInterval(Integer frequencyInterval) {
        this.frequencyInterval = frequencyInterval;
    }

    public String getScheduleDays() {
        return scheduleDays;
    }

    public void setScheduleDays(String scheduleDays) {
        this.scheduleDays = scheduleDays;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
