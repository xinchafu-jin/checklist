package com.example.checklist.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "question_option")
public class QuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "question_id", nullable = false)
    private Long questionId; // 直接記錄 questionId
    @Column(name = "option_code", length = 10)
    private String optionCode; // 選項編號 (如 A, B, C)
    @Column(name = "option_text", nullable = false)
    private String optionText; // 選項內容 (如 非常滿意)
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getQuestionId() {
        return questionId;
    }
    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }
    public String getOptionCode() {
        return optionCode;
    }
    public void setOptionCode(String optionCode) {
        this.optionCode = optionCode;
    }
    public String getOptionText() {
        return optionText;
    }
    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }
}
