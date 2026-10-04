package com.example.checklist.repository;

import com.example.checklist.entity.QuizResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List; // 儲存查詢回傳的多筆作答主檔

public interface QuizResponseRepository extends JpaRepository<QuizResponse, Long> {

    /**
     * 新增一筆問卷回覆主檔（記錄是誰、填了哪份問卷）。
     *
     * @param quizId    填寫的問卷 id
     * @param userEmail 填答者 email
     */
    @Modifying
    @Query(value = "INSERT INTO quiz_response (quiz_id, user_email) " //
            + " VALUES (:quizId, :userEmail)", nativeQuery = true)
    public void insertQuizResponse(//
                                   @Param("quizId") Long quizId, //
                                   @Param("userEmail") String userEmail);

    /**
     * 取得剛剛 insert 產生的自增主鍵，用來當作明細的 response_id。
     *
     * @return 最後一次 INSERT 的自增 id
     */
    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    public Long getLastInsertedId();

    /**
     * 依據問卷 id 查出所有填答紀錄（依提交時間遞減排序）。
     *
     * @param quizId 問卷 id
     * @return 該問卷的所有作答主檔
     */
    @Query(value = "SELECT * FROM quiz_response WHERE quiz_id = ?1 " //
            + " ORDER BY submitted_at DESC", nativeQuery = true)
    public List<QuizResponse> findByQuizId(Long quizId);


    @Query(value = "SELECT quiz_id FROM quiz_response WHERE user_email = ?1", nativeQuery = true)
    public List<Long> findQuizIdsByUserEmail(String userEmail);
}
