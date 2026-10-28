package com.interviewbank.web;

import com.interviewbank.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return statsService.dashboard();
    }

    @GetMapping("/insights")
    public Map<String, Object> insights() {
        return statsService.insights();
    }
}
