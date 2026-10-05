package com.example.checklist.entity;

import jakarta.persistence.*;

import java.time.LocalTime;

/**
 * 班別。end_time 早於 start_time 代表跨午夜，檢點日算開班那天。
 */
@Entity
@Table(name = "shift", uniqueConstraints = {
        @UniqueConstraint(name = "uk_shift_seq", columnNames = {"seq"})})
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "name", nullable = false, length = 20)
    private String name; // 早班 / 中班 / 夜班
    @Column(name = "seq", nullable = false)
    private Integer seq; // 第幾班，每班檢點的 period_key 用它，例如 2026-10-03#2
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
