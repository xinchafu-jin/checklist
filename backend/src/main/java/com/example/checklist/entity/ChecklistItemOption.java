package com.example.checklist.entity;

import com.example.checklist.enums.ResultLevel;
import jakarta.persistence.*;

/**
 * 單選、多選項目的選項。畫面顯示 label，判斷與統計看 resultLevel；每項至少要有一個 NORMAL，由 service 檢查。
 */
@Entity
@Table(name = "checklist_item_option", uniqueConstraints = {
        @UniqueConstraint(name = "uk_option_seq", columnNames = {"item_id", "seq"})})
public class ChecklistItemOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "item_id", nullable = false)
    private Long itemId;
    @Column(name = "seq", nullable = false)
    private Integer seq;
    @Column(name = "label", nullable = false, length = 100)
    private String label; // 例如 輕微磨損
    @Enumerated(EnumType.STRING)
    @Column(name = "result_level", nullable = false, length = 20)
    private ResultLevel resultLevel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ResultLevel getResultLevel() {
        return resultLevel;
    }

    public void setResultLevel(ResultLevel resultLevel) {
        this.resultLevel = resultLevel;
    }
}
