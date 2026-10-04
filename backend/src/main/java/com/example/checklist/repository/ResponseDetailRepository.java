package com.example.checklist.repository;

import com.example.checklist.entity.ResponseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List; // 儲存查詢回傳的多筆作答明細

public interface ResponseDetailRepository extends JpaRepository<ResponseDetail, Long> {

    /**
     * 新增一筆作答明細（某份回覆中，某題選了哪個選項或填了什麼文字）。
     *
     * @param responseId 對應的問卷回覆主檔 id
     * @param questionId 作答的題目 id
     * @param optionId   選取的選項 id；簡答題可為 null
     * @param answerText 簡答題填答文字；選擇題可為 null
     */
    @Modifying
    @Query(value = "INSERT INTO response_detail (response_id, question_id, option_id, answer_text) "//
            + " VALUES (:responseId, :questionId, :optionId, :answerText)", nativeQuery = true)
    public void insertDetail(//
            @Param("responseId") Long responseId, //
            @Param("questionId") Long questionId, //
            @Param("optionId") Long optionId, //
            @Param("answerText") String answerText);

    /**
     * 依據 response_id 查出該次提交的所有回答細節。
     *
     * @param responseId 作答主檔 id
     * @return 該次提交的所有明細
     */
    @Query(value = "SELECT * FROM response_detail WHERE response_id = ?1", nativeQuery = true)
    public List<ResponseDetail> findByResponseId(Long responseId);
}
