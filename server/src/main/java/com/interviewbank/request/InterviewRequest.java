package com.interviewbank.request;

import com.interviewbank.domain.InterviewResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record InterviewRequest(
        @NotBlank(message = "不能为空") @Size(max = 120) String company,
        @Size(max = 120) String position,
        @Size(max = 40) String round,
        LocalDate interviewDate,
        InterviewResult result,
        @Size(max = 40) String channel,
        @Size(max = 60) String salaryRange,
        String summary,
        Integer selfRating,
        List<QuestionLink> questions
) {
    public record QuestionLink(Long questionId, String note) {
    }
}
