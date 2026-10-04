package com.example.checklist.repository;

import com.example.checklist.entity.QuestionOption; // 指定此 Repository 要操作的 QuestionOption Entity
import org.springframework.data.jpa.repository.JpaRepository; // 提供選項資料的基本 CRUD 方法
import org.springframework.data.jpa.repository.Modifying; // 標記會修改資料庫內容的 SQL
import org.springframework.data.jpa.repository.Query; // 允許在方法上定義原生 SQL
import org.springframework.data.repository.query.Param; // 綁定方法參數與 SQL 的 :命名參數
import org.springframework.stereotype.Repository; // 將資料存取元件交給 Spring 管理

import java.util.List; // 儲存查詢回傳的多筆選項

/**
 * 題目選項資料表的 Repository。
 *
 * <p>{@code @Repository} 表示這是資料存取層元件。
 * Spring Data JPA 本身也會掃描 JpaRepository 介面，因此此標籤主要用來清楚表達角色。</p>
 */
@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {

    /**
     * 查詢指定題目的全部選項，並依 id 由小到大排列。
     *
     * @param questionId 題目 id
     * @return 該題目的選項清單
     */
    @Query(value = "SELECT * FROM question_option WHERE question_id = ?1 ORDER BY id ASC", //
            nativeQuery = true)
    public List<QuestionOption> findByQuestionId(Long questionId);

    /**
     * 新增一筆題目選項。
     *
     * @param questionId 選項所屬的題目 id
     * @param optionCode 選項代碼，例如 A、B、C
     * @param optionText 選項顯示文字
     */
    @Modifying
    @Query(value = "INSERT INTO question_option (question_id, option_code, option_text) "//
            + " VALUES (:questionId, :optionCode, :optionText)", nativeQuery = true)
    public void insertOption(//
                             @Param("questionId") Long questionId, //
                             @Param("optionCode") String optionCode,//
                             @Param("optionText") String optionText);

    /**
     * 刪除指定問卷底下所有題目的選項。
     *
     * <p>子查詢先從 question 找出屬於該問卷的題目 id，
     * 外層 DELETE 再刪除 question_option 中對應的選項。</p>
     *
     * @param quizId 問卷 id
     */
    @Modifying
    @Query(value = "DELETE FROM question_option WHERE question_id "
            + " IN (SELECT id FROM question WHERE quiz_id = ?1)", nativeQuery = true)
    public void deleteByQuizId(Long quizId);


    // question_option 綁定 question_id，question_id 則是從 quiz_id 得到
    @Modifying
    @Query(value = "DELETE FROM question_option WHERE question_id IN ("//
            + " SELECT id FROM question WHERE quiz_id IN (:quizIds))", //
            nativeQuery = true)
    public void deleteByQuizIds(@Param("quizIds") List<Long> quizIds);

}
