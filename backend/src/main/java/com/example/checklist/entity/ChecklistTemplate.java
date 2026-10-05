package com.example.checklist.entity;

import com.example.checklist.enums.TemplateKind;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 檢點範本，例如「堆高機每日作業前檢點」。檢點項目掛在版本底下，見 {@link TemplateVersion}。
 */
@Entity
@Table(name = "checklist_template", uniqueConstraints = {
        @UniqueConstraint(name = "uk_template_code", columnNames = {"code"})})
public class ChecklistTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "code", nullable = false, length = 30)
    private String code; // 表單編號
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 20)
    private TemplateKind kind = TemplateKind.INSPECTION;
    @Column(name = "legal_basis", length = 255)
    private String legalBasis; // 法規依據
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TemplateKind getKind() {
        return kind;
    }

    public void setKind(TemplateKind kind) {
        this.kind = kind;
    }

    public String getLegalBasis() {
        return legalBasis;
    }

    public void setLegalBasis(String legalBasis) {
        this.legalBasis = legalBasis;
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
