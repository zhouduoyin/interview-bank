package com.interviewbank.request;

import com.interviewbank.domain.Mastery;

import java.util.List;

public record BatchRequest(List<Long> ids, String action, Mastery mastery, Long categoryId) {
}
