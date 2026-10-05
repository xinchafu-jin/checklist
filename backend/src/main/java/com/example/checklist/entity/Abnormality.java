package com.example.checklist.entity;

import com.example.checklist.enums.AbnormalityStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 異常單。結果為 ATTENTION 或 ABNORMAL 時自動開立；actionTaken 就是法規要求記錄的「改善措施」。
 */
@Entity
@Table(name = "abnormality", uniqueConstraints = {
        @UniqueConstraint(name = "uk_abnormality_result", columnNames = {"result_id"})})
public class Abnormality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "result_id", nullable = false)
    private Long resultId; // 由哪一項結果開出
    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId; // 冗餘存一份，依設備查異常比較快
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AbnormalityStatus status = AbnormalityStatus.OPEN;
    @Column(name = "action_taken", length = 1000)
    private String actionTaken; // 處置內容（改善措施）
    @Column(name = "closed_by")
    private Long closedBy;
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
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

    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public AbnormalityStatus getStatus() {
        return status;
    }

    public void setStatus(AbnormalityStatus status) {
        this.status = status;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public Long getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(Long closedBy) {
        this.closedBy = closedBy;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
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
