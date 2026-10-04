package com.example.checklist.dto.request;

import com.example.checklist.constants.ValidationMsg; // 共用的 DTO 驗證失敗訊息
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid; // 讓驗證繼續往內檢查每個 QuestionRequest
import jakarta.validation.constraints.NotBlank; // 驗證字串不可為 null、空字串或只有空白
import jakarta.validation.constraints.NotNull; // 驗證日期與 Boolean 等欄位不可為 null

import java.time.LocalDate; // 表示只有年月日、不含時間與時區的日期
import java.util.List; // 儲存一份問卷中的多個題目

/**
 * 前端新增或更新問卷時送入 Controller 的最外層 Request DTO。
 *
 * <p>JSON 會先由 Jackson 轉成此物件，再由 {@code @Valid} 執行欄位驗證。</p>
 */
public class QuizRequest {

    /*
     * create 與 update 共用 QuizRequest：
     * 新增問卷時不需要傳 id，此欄位會是 null；
     * 更新問卷時必須傳入要更新的問卷 id。
     *
     * 使用 Long 而不是 long，是因為 Long 可以用 null 表示「前端沒有提供 id」。
     */
    private Long id;

    // 問卷標題不能是 null、空字串或只有空白。
    @NotBlank(message = ValidationMsg.QUIZ_TITLE_REQUIRED)
    private String title;

    // 問卷說明也必須有實際文字。
    @NotBlank(message = ValidationMsg.QUIZ_DESCRIPTION_REQUIRED)
    private String description;

    // @NotNull 只檢查有沒有提供日期；日期先後順序由 QuizService 驗證。
    @NotNull(message = ValidationMsg.QUIZ_START_DATE_REQUIRED)
    private LocalDate startDate;

    @NotNull(message = ValidationMsg.QUIZ_END_DATE_REQUIRED)
    private LocalDate endDate;

    // 前端省略此欄位時預設為 false；Boolean 仍可能被明確設成 null，所以保留 @NotNull。
    @NotNull(message = ValidationMsg.QUIZ_PUBLISHED_REQUIRED)
    private Boolean isPublished = false;

    private String creatorEmail;

    public Boolean getPublished() {
        return isPublished;
    }

    public void setPublished(Boolean published) {
        isPublished = published;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    // 逐一驗證 questions 中每個 QuestionRequest 及其內部 OptionRequest。
    @Valid
    List<QuestionRequest> questions;

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
     * @param startDate 要設定的開始日期，JSON 格式通常為 yyyy-MM-dd
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
     * @return 是否發布問卷
     */
    public Boolean getIsPublished() {
        return isPublished;
    }

    /**
     * @param isPublished true 表示發布，false 表示尚未發布
     */
    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    /**
     * @return 問卷內的題目清單
     */
    public List<QuestionRequest> getQuestions() {
        return questions;
    }

    /**
     * @param questions 要設定的題目清單
     */
    public void setQuestions(List<QuestionRequest> questions) {
        this.questions = questions;
    }

    /**
     * @return 問卷 id；新增時可能為 null，更新時必須有值
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id 更新時要指定的問卷 id
     */
    public void setId(Long id) {
        this.id = id;
    }
}
