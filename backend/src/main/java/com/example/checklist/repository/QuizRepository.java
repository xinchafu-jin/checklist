package com.example.checklist.repository;

import com.example.checklist.entity.Quiz; // 指定此 Repository 要操作的 Quiz Entity
import org.springframework.data.jpa.repository.JpaRepository; // 提供 save、findById、findAll、delete 等基本 CRUD
import org.springframework.data.jpa.repository.Modifying; // 告訴 Spring Data 此查詢會修改資料
import org.springframework.data.jpa.repository.Query; // 在 Repository 方法上撰寫 JPQL 或原生 SQL
import org.springframework.data.repository.query.Param; // 將 Java 方法參數綁定到 SQL 的命名參數

import java.time.LocalDate; // 對應 SQL 的 DATE 欄位
import java.util.List; // 表示查詢可能回傳多筆 Quiz
import java.util.Optional; // 包裝「可能查到，也可能查不到」的單筆結果

/**
 * 問卷資料表的 Repository（資料存取層）。
 *
 * <p>{@code JpaRepository<Quiz, Long>} 的第一個泛型是要操作的 Entity，
 * 第二個泛型是該 Entity 主鍵 {@code id} 的 Java 型別。</p>
 *
 * <p>Spring Data JPA 會在程式啟動時自動建立此介面的實作物件，
 * 所以不需要自己撰寫 {@code new QuizRepository()}。</p>
 */
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    /**
     * 查詢所有問卷，並按照 id 由大到小排序。
     *
     * <p>{@code nativeQuery = true} 表示 Query 內容是資料庫原生 SQL，
     * 查詢的是資料表名稱 {@code quiz}，而不是 Java 類別名稱。</p>
     *
     * @return 所有問卷，最新新增的排在前面
     */
    @Query(value = "SELECT * FROM quiz ORDER BY id DESC", nativeQuery = true)
    public List<Quiz> findAllQuizzes();

    /**
     * 依主鍵查詢一份問卷。
     *
     * <p>SQL 中的 {@code ?1} 代表方法的第 1 個參數，也就是 id。
     * Optional 有值表示查到，空的 Optional 表示查不到。</p>
     *
     * @param id 問卷主鍵
     * @return 可能包含 Quiz 的 Optional
     */
    @Query(value = "SELECT * FROM quiz WHERE id = ?1", nativeQuery = true)
    public Optional<Quiz> findQuizById(Long id);

    /**
     * 新增問卷資料。
     *
     * <p>{@code @Modifying} 必須加在 INSERT、UPDATE、DELETE 上，
     * 否則 Spring Data 會把 SQL 當成一般 SELECT 查詢。</p>
     *
     * <p>SQL 中的 {@code :title} 是命名參數，
     * 會與 {@code @Param("title")} 標記的方法參數配對。</p>
     *
     * @param title       問卷標題
     * @param description 問卷說明
     * @param startDate   開始日期
     * @param endDate     結束日期
     * @param isPublished 是否發布
     */
    @Modifying
    @Query(value = "INSERT INTO quiz (title, description, start_date, end_date, is_published,creator_email) "//
            + " VALUES (:title, :description, :startDate, :endDate, :isPublished, :creatorEmail)", //
            nativeQuery = true)
    public void insertQuiz(//
                           @Param("title") String title, //
                           @Param("description") String description,//
                           @Param("startDate") LocalDate startDate, //
                           @Param("endDate") LocalDate endDate,//
                           @Param("isPublished") Boolean isPublished,
                            @Param("creatorEmail") String creatorEmail);

    /**
     * 依 id 更新問卷欄位。
     *
     * <p><b>{@code creator_email} 刻意不在這句 SQL 裡。</b>
     * 加進來的話，任何人編輯一次問卷，製作者就會變成編輯者。
     * 製作者只在 {@link #insertQuiz} 時寫入一次，之後不再改動。</p>
     *
     * <p>{@code updated_at} 則是明確寫入 {@code CURRENT_TIMESTAMP}。
     * 資料表雖然有 {@code ON UPDATE CURRENT_TIMESTAMP}，但那只在
     * 「至少一個欄位的值真的改變」時才觸發 —— 使用者若只改了題目
     * （題目在 question 表），這裡五個欄位的值都沒變，時間就不會更新。
     * 明確寫入代表「走過一次 updateQuiz 就算一次更新」。</p>
     *
     * @param id          要更新的問卷 id
     * @param title       新標題
     * @param description 新說明
     * @param startDate   新開始日期
     * @param endDate     新結束日期
     * @param isPublished 新發布狀態
     * @return 實際受影響的資料筆數；通常成功為 1，找不到 id 為 0
     */
    @Modifying
    @Query(value = "UPDATE quiz SET title = :title, description = :description, "//
            + " start_date = :startDate, end_date = :endDate, is_published = :isPublished, "//
            + " updated_at = CURRENT_TIMESTAMP "//
            + " WHERE id = :id", nativeQuery = true)
    public int updateQuiz(//
                          @Param("id") Long id, //
                          @Param("title") String title, //
                          @Param("description") String description,//
                          @Param("startDate") LocalDate startDate, //
                          @Param("endDate") LocalDate endDate,//
                          @Param("isPublished") Boolean isPublished);

    /**
     * 取得目前 MySQL 連線最後一次 INSERT 產生的自動流水號。
     *
     * <p>Service 取得此 id 後，會把它設定為 Question 的 quiz_id。
     * 此方法依賴 MySQL 的 {@code LAST_INSERT_ID()} 函式與同一筆交易連線。</p>
     *
     * @return 最近新增問卷的 id
     */
    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    public Long getLastInsertedId();


    // quizIds= [1, 3, 4, 5]
    // SQL 語法: DELETE FROM quiz WHERE id IN (1, 3, 4, 5)，IN 後面記得要加上小括號
    @Modifying
    @Query(value = "DELETE FROM quiz WHERE id IN (:quizIds)", nativeQuery = true)
    public int deleteByQuizIds(@Param("quizIds") List<Long> quizIds);
}
