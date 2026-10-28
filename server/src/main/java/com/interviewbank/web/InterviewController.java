package com.interviewbank.web;

import com.interviewbank.domain.InterviewRecord;
import com.interviewbank.request.InterviewRequest;
import com.interviewbank.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping
    public List<Map<String, Object>> list() {
        return interviewService.list();
    }

    @GetMapping("/{id}")
    public InterviewRecord get(@PathVariable Long id) {
        return interviewService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InterviewRecord create(@Valid @RequestBody InterviewRequest req) {
        return interviewService.create(req);
    }

    @PutMapping("/{id}")
    public InterviewRecord update(@PathVariable Long id, @Valid @RequestBody InterviewRequest req) {
        return interviewService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        interviewService.delete(id);
        return Map.of("ok", true);
    }

    @PostMapping("/{id}/questions")
    public InterviewRecord attach(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long questionId = body.get("questionId") == null ? null : Long.valueOf(body.get("questionId").toString());
        String note = body.get("note") == null ? null : body.get("note").toString();
        return interviewService.attachQuestion(id, questionId, note);
    }

    @DeleteMapping("/{id}/questions/{linkId}")
    public InterviewRecord detach(@PathVariable Long id, @PathVariable Long linkId) {
        return interviewService.detachQuestion(id, linkId);
    }
}
