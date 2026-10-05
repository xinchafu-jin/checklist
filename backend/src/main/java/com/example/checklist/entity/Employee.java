package com.example.checklist.entity;

import com.example.checklist.enums.EmployeeRole;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 員工。用工號登入；離職把 active 設為 false，不刪除，舊紀錄才查得到是誰檢的。
 */
@Entity
@Table(name = "employee", uniqueConstraints = {
        @UniqueConstraint(name = "uk_employee_no", columnNames = {"employee_no"})})
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "employee_no", nullable = false, length = 20)
    private String employeeNo; // 工號，登入帳號
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash; // BCrypt 雜湊
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private EmployeeRole role;
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

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public EmployeeRole getRole() {
        return role;
    }

    public void setRole(EmployeeRole role) {
        this.role = role;
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
