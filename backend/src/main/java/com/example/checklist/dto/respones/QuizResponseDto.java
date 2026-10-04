package com.example.checklist.dto.respones;

import java.time.LocalDate; // 表示問卷開始與結束的年月日
import java.time.LocalDateTime;
import java.util.List; // 儲存問卷內的多個題目 DTO

/**
 * 回傳給前端的問卷 Response DTO。
 *
 * <p>列表 API 只填入問卷基本資料並將 questions 設為 null；
 * 需要題目時則可使用 {@code List<QuestionResponse>} 表示巢狀資料。</p>
 */
public class QuizResponseDto {

    // 問卷主鍵。
    private Long id;

    // 問卷標題。
    private String title;

    // 問卷說明。
    private String description;

    // 問卷開始日期。
    private LocalDate startDate;

    // 問卷結束日期。
    private LocalDate endDate;

    // 是否已發布。
    private Boolean isPublished;

    // 問卷內的題目清單；不需要明細時可以是 null。
    private List<QuestionResponse> questions;

    private String creatorEmail;

    /*
     * 製作者姓名，給畫面顯示用。
     *
     * quiz 表只存 creator_email，沒有姓名；這個欄位由 QuizService
     * 查 user 表現算後填入，資料庫裡不存在對應的欄位。
     * 姓名不落地，所以使用者改名的隔天，同一支 API 就會回新名字。
     */
    private String creatorName;

    /*
     * 建立與最後更新時間，資料庫自動維護。
     *
     * 欄位名要與 JSON 完全一致：Jackson 是看 getter 名稱決定 JSON 的 key
     * （getUpdatedAt → updatedAt），拼錯的話前端拿到的是 undefined 而且不會報錯。
     */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    /**
     * @return 問卷 id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id 要設定的問卷 id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return 問卷標題
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title 要設定的問卷標題
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return 問卷說明
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description 要設定的問卷說明
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return 問卷開始日期
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * @param startDate 要設定的開始日期
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * @return 問卷結束日期
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * @param endDate 要設定的結束日期
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * @return 是否發布
     */
    public Boolean getIsPublished() {
        return isPublished;
    }

    /**
     * @param isPublished 是否發布
     */
    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    /**
     * @return 問卷內的題目清單
     */
    public List<QuestionResponse> getQuestions() {
        return questions;
    }

    /**
     * @param questions 要設定的題目清單
     */
    public void setQuestions(List<QuestionResponse> questions) {
        this.questions = questions;
    }
}
