package com.example.checklist.repository;

import com.example.checklist.entity.Question; // 指定此 Repository 要操作的 Question Entity
import org.springframework.data.jpa.repository.JpaRepository; // 提供 Question 的基本 CRUD 方法
import org.springframework.data.jpa.repository.Modifying; // 標記會執行 INSERT、UPDATE 或 DELETE 的查詢
import org.springframework.data.jpa.repository.Query; // 允許直接在方法上撰寫查詢
import org.springframework.data.repository.query.Param; // 綁定 Java 參數與 SQL 命名參數

import java.util.List; // 儲存查詢回傳的多筆題目

/**
 * 題目資料表的 Repository。
 *
 * <p>{@code JpaRepository<Question, Long>} 表示操作 Question Entity，
 * 而它的主鍵型別是 Long。Spring Data 會自動產生介面實作。</p>
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * 查詢指定問卷的全部題目，並依題號由小到大排序。
     *
     * @param quizId 問卷 id
     * @return 該問卷的題目清單
     */
    @Query(value = "SELECT * FROM question WHERE quiz_id = ?1 ORDER BY question_num ASC", //
            nativeQuery = true)
    public List<Question> findByQuizId(Long quizId);

    /**
     * 新增一筆題目。
     *
     * <p>{@code @Modifying} 告訴 Spring 這不是 SELECT；
     * 每一個 {@code @Param} 名稱都必須和 SQL 中冒號後面的名稱完全一致。</p>
     *
     * @param quizId      題目所屬問卷 id
     * @param questionNum 題號
     * @param title       題目文字
     * @param type        題型字串，例如 SINGLE
     * @param isRequired  是否必填
     */
    @Modifying
    @Query(value = "INSERT INTO question (quiz_id, question_num, title, type, is_required) "//
            + " VALUES (:quizId, :questionNum, :title, :type, :isRequired)", nativeQuery = true)
    public void insertQuestion(//
                               @Param("quizId") Long quizId, //
                               @Param("questionNum") Integer questionNum,//
                               @Param("title") String title, //
                               @Param("type") String type, //
                               @Param("isRequired") Boolean isRequired);

    /**
     * 刪除指定問卷底下的所有題目。
     *
     * <p>這是修改資料的 DELETE SQL，因此同樣需要 {@code @Modifying}，
     * 並且應該在 Service 的 {@code @Transactional} 方法中呼叫。</p>
     *
     * @param quizId 問卷 id
     */
    @Modifying
    @Query(value = "DELETE FROM question WHERE quiz_id = ?1", nativeQuery = true)
    public void deleteByQuizId(Long quizId);

    /**
     * 取得 MySQL 目前連線最後新增資料產生的 id。
     *
     * @return 最近新增題目的 id
     */
    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    public Long getLastInsertedId();


    @Modifying
    @Query(value = "DELETE FROM question WHERE quiz_id IN (:quizIds)", nativeQuery = true)
    public void deleteByQuizIds(@Param("quizIds") List<Long> quizIds);
}
