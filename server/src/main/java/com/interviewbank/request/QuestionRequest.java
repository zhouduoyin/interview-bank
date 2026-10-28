package com.interviewbank.request;

import com.interviewbank.domain.Difficulty;
import com.interviewbank.domain.Mastery;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record QuestionRequest(
        @NotBlank(message = "不能为空") @Size(max = 300, message = "长度不能超过 300") String title,
        String content,
        String answer,
        String note,
        Long categoryId,
        Difficulty difficulty,
        Mastery mastery,
        Set<String> tags,
        @Size(max = 120) String sourceCompany,
        @Size(max = 120) String sourcePosition,
        Boolean starred
) {
}
