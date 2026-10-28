package com.interviewbank.web;

import com.interviewbank.common.PageResult;
import com.interviewbank.domain.Difficulty;
import com.interviewbank.domain.Mastery;
import com.interviewbank.domain.Question;
import com.interviewbank.domain.ReviewLog;
import com.interviewbank.request.BatchRequest;
import com.interviewbank.request.QuestionRequest;
import com.interviewbank.request.ReviewRequest;
import com.interviewbank.service.InterviewService;
import com.interviewbank.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final InterviewService interviewService;

    @GetMapping
    public PageResult<Question> list(@RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) Long categoryId,
                                     @RequestParam(required = false) Difficulty difficulty,
                                     @RequestParam(required = false) Mastery mastery,
                                     @RequestParam(required = false) String tag,
                                     @RequestParam(required = false) Boolean starred,
                                     @RequestParam(required = false, defaultValue = "updated") String sort,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return questionService.search(keyword, categoryId, difficulty, mastery, tag, starred, sort, page, size);
    }

    @GetMapping("/recent")
    public List<Question> recent(@RequestParam(defaultValue = "6") int limit) {
        return questionService.recent(Math.min(limit, 20));
    }

    @GetMapping("/{id}")
    public Question get(@PathVariable Long id) {
        return questionService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Question create(@Valid @RequestBody QuestionRequest req) {
        return questionService.create(req);
    }

    @PutMapping("/{id}")
    public Question update(@PathVariable Long id, @Valid @RequestBody QuestionRequest req) {
        return questionService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Map.of("ok", true);
    }

    @PatchMapping("/{id}/star")
    public Question star(@PathVariable Long id) {
        return questionService.toggleStar(id);
    }

    @PatchMapping("/{id}/mastery")
    public Question mastery(@PathVariable Long id, @RequestParam Mastery value) {
        return questionService.setMastery(id, value);
    }

    @PostMapping("/{id}/review")
    public Question review(@PathVariable Long id, @Valid @RequestBody ReviewRequest req) {
        return questionService.review(id, req.rating());
    }

    @GetMapping("/{id}/reviews")
    public List<ReviewLog> reviews(@PathVariable Long id) {
        return questionService.reviewLogs(id);
    }

    @GetMapping("/{id}/interviews")
    public List<Map<String, Object>> interviews(@PathVariable Long id) {
        return interviewService.listByQuestion(id);
    }

    @PostMapping("/batch")
    public Map<String, Object> batch(@RequestBody BatchRequest req) {
        return questionService.batch(req);
    }
}
