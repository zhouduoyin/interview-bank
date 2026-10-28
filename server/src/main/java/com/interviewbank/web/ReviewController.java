package com.interviewbank.web;

import com.interviewbank.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final QuestionService questionService;

    @GetMapping("/queue")
    public Map<String, Object> queue(@RequestParam(defaultValue = "20") int limit) {
        return questionService.reviewQueue(limit);
    }
}
