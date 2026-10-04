package com.example.checklist.entity;

import com.example.checklist.enums.QuestionType;
import jakarta.persistence.*;

@Entity
@Table(name = "question")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "quiz_id", nullable = false)
    private Long quizId; // 直接放 quizId，方便 SQL 操作
    @Column(name = "question_num", nullable = false)
    private Integer questionNum;
    @Column(name = "title", nullable = false)
    private String title;


    /*@Enumerated(EnumType.STRING): 將 Java 的 Enum（列舉）轉成『字串名稱』存入資料庫，
     * 而不是『數字索引（0, 1, 2...）』*/


    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private QuestionType type;
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public Integer getQuestionNum() {
        return questionNum;
    }

    public void setQuestionNum(Integer questionNum) {
        this.questionNum = questionNum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

}
