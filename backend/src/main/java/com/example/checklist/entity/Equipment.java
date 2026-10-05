package com.example.checklist.entity;

import com.example.checklist.enums.EquipmentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 設備。
 */
@Entity
@Table(name = "equipment", uniqueConstraints = {
        @UniqueConstraint(name = "uk_equipment_code", columnNames = {"code"})})
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "code", nullable = false, length = 30)
    private String code; // 設備編號，例如 FL-03
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    @Column(name = "category", length = 50)
    private String category;
    @Column(name = "area_id", nullable = false)
    private Long areaId;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EquipmentStatus status = EquipmentStatus.IN_SERVICE;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
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
