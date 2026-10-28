package com.interviewbank.request;

import com.interviewbank.domain.Rating;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(@NotNull(message = "不能为空") Rating rating) {
}
