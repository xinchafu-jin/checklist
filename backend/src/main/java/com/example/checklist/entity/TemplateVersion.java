package com.example.checklist.entity;

import com.example.checklist.enums.VersionStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 範本版本。改項目就發新版本，舊紀錄仍對應當時的版本；同一範本同時只能有一個 PUBLISHED，由 service 檢查。
 */
@Entity
@Table(name = "template_version", uniqueConstraints = {
        @UniqueConstraint(name = "uk_version_no", columnNames = {"template_id", "version_no"})})
public class TemplateVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "template_id", nullable = false)
    private Long templateId;
    @Column(name = "version_no", nullable = false)
    private Integer versionNo;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VersionStatus status = VersionStatus.DRAFT;
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
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

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public VersionStatus getStatus() {
        return status;
    }

    public void setStatus(VersionStatus status) {
        this.status = status;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
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
