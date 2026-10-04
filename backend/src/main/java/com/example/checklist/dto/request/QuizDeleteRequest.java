package com.example.checklist.dto.request;

import com.example.checklist.constants.ValidationMsg;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class QuizDeleteRequest {
    @NotEmpty(message = ValidationMsg.QUIZ_IDS_REQUIRED)
    private List<Long> quizIds;
    public List<Long> getQuizIds() {
        return quizIds;
    }
    public void setQuizIds(List<Long> quizIds) {
        this.quizIds = quizIds;
    }

}
