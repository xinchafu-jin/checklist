package com.example.checklist.entity;

import com.example.checklist.enums.RecordStatus;
import com.example.checklist.enums.ResultLevel;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 檢點紀錄，等於紙本月表上的一格。唯一鍵是 設備 + 範本 + periodKey，送出後不能改、不能刪。
 */
@Entity
@Table(name = "inspection_record", uniqueConstraints = {
        @UniqueConstraint(name = "uk_record_period", columnNames = {"equipment_id", "template_id", "period_key"})})
public class InspectionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;
    @Column(name = "template_id", nullable = false)
    private Long templateId; // 唯一鍵用
    @Column(name = "template_version_id", nullable = false)
    private Long templateVersionId; // 依據哪個版本檢點
    @Column(name = "period_key", nullable = false, length = 20)
    private String periodKey; // 例如 2026-10-03、2026-10-03#2、2026-W40
    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate; // 檢點日（Asia/Taipei）
    @Column(name = "shift_id")
    private Long shiftId; // 每班檢點才有
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status;
    @Enumerated(EnumType.STRING)
    @Column(name = "overall_result", length = 20)
    private ResultLevel overallResult; // 停機時為 null
    @Column(name = "remark", length = 500)
    private String remark; // 建議改善事項
    @Column(name = "inspector_id", nullable = false)
    private Long inspectorId;
    @Column(name = "submitted_at", insertable = false, updatable = false)
    private LocalDateTime submittedAt; // 資料庫自動填入
    @Column(name = "reviewed_by")
    private Long reviewedBy; // 主管覆核
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

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

    public Long getTemplateVersionId() {
        return templateVersionId;
    }

    public void setTemplateVersionId(Long templateVersionId) {
        this.templateVersionId = templateVersionId;
    }

    public String getPeriodKey() {
        return periodKey;
    }

    public void setPeriodKey(String periodKey) {
        this.periodKey = periodKey;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }

    public RecordStatus getStatus() {
        return status;
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
    }

    public ResultLevel getOverallResult() {
        return overallResult;
    }

    public void setOverallResult(ResultLevel overallResult) {
        this.overallResult = overallResult;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getInspectorId() {
        return inspectorId;
    }

    public void setInspectorId(Long inspectorId) {
        this.inspectorId = inspectorId;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
