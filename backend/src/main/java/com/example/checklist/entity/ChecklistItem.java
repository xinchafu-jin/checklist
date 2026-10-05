package com.example.checklist.entity;

import com.example.checklist.enums.ItemType;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * 檢點項目。數值項目用 min/max 判斷，超出算異常；單選、多選項目的選項見 {@link ChecklistItemOption}。
 */
@Entity
@Table(name = "checklist_item", uniqueConstraints = {
        @UniqueConstraint(name = "uk_item_seq", columnNames = {"version_id", "seq"})})
public class ChecklistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "version_id", nullable = false)
    private Long versionId;
    @Column(name = "seq", nullable = false)
    private Integer seq; // 排列順序
    @Column(name = "section", length = 50)
    private String section; // 部位，例如 驅動、電氣
    @Column(name = "name", nullable = false, length = 200)
    private String name; // 例如 煞車功能是否正常
    @Column(name = "method", length = 100)
    private String method; // 檢點方法，例如 目視
    @Column(name = "standard", length = 200)
    private String standard; // 判定基準，例如 無漏油
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private ItemType itemType;
    @Column(name = "min_value", precision = 12, scale = 3)
    private BigDecimal minValue;
    @Column(name = "max_value", precision = 12, scale = 3)
    private BigDecimal maxValue;
    @Column(name = "unit", length = 20)
    private String unit; // 例如 kgf/cm²

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
