package com.interviewbank.service;

import com.interviewbank.common.ApiException;
import com.interviewbank.domain.*;
import com.interviewbank.repo.InterviewQuestionRepository;
import com.interviewbank.repo.InterviewRecordRepository;
import com.interviewbank.repo.QuestionRepository;
import com.interviewbank.request.InterviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRecordRepository recordRepository;
    private final InterviewQuestionRepository linkRepository;
    private final QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (InterviewRecord r : recordRepository.findAllByOrderByInterviewDateDescIdDesc()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", r.getId());
            map.put("company", r.getCompany());
            map.put("position", r.getPosition());
            map.put("round", r.getRound());
            map.put("interviewDate", r.getInterviewDate());
            map.put("result", r.getResult());
            map.put("channel", r.getChannel());
            map.put("salaryRange", r.getSalaryRange());
            map.put("selfRating", r.getSelfRating());
            map.put("questionCount", r.getQuestions().size());
            map.put("createdAt", r.getCreatedAt());
            map.put("updatedAt", r.getUpdatedAt());
            map.put("demo", r.isDemo());
            map.put("questionTitles", r.getQuestions().stream()
                    .map(iq -> iq.getQuestion().getTitle())
                    .limit(4)
                    .toList());
            String summary = r.getSummary();
            map.put("summaryExcerpt", summary == null || summary.isBlank() ? "" :
                    summary.replaceAll("[#>*`\\-\\[\\]()!|]", " ").replaceAll("\\s+", " ").trim()
                            .substring(0, Math.min(90, summary.replaceAll("[#>*`\\-\\[\\]()!|]", " ")
                                    .replaceAll("\\s+", " ").trim().length())));
            result.add(map);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public InterviewRecord get(Long id) {
        return recordRepository.findById(id).orElseThrow(() -> ApiException.notFound("面试记录"));
    }

    @Transactional
    public InterviewRecord create(InterviewRequest req) {
        InterviewRecord r = new InterviewRecord();
        apply(r, req);
        r.setCreatedAt(Instant.now());
        return recordRepository.save(r);
    }

    @Transactional
    public InterviewRecord update(Long id, InterviewRequest req) {
        InterviewRecord r = get(id);
        r.getQuestions().clear();
        apply(r, req);
        return recordRepository.save(r);
    }

    private void apply(InterviewRecord r, InterviewRequest req) {
        r.setCompany(req.company().trim());
        r.setPosition(req.position());
        r.setRound(req.round());
        r.setInterviewDate(req.interviewDate() == null ? LocalDate.now() : req.interviewDate());
        r.setResult(req.result() == null ? InterviewResult.PENDING : req.result());
        r.setChannel(req.channel());
        r.setSalaryRange(req.salaryRange());
        r.setSummary(req.summary());
        r.setSelfRating(req.selfRating());
        if (req.questions() != null) {
            int index = 0;
            for (InterviewRequest.QuestionLink link : req.questions()) {
                if (link == null || link.questionId() == null) continue;
                Optional<Question> q = questionRepository.findById(link.questionId());
                if (q.isEmpty()) continue;
                r.getQuestions().add(new InterviewQuestion(r, q.get(), link.note(), index++));
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        recordRepository.delete(get(id));
    }

    @Transactional
    public InterviewRecord attachQuestion(Long recordId, Long questionId, String note) {
        InterviewRecord r = get(recordId);
        Question q = questionRepository.findById(questionId).orElseThrow(() -> ApiException.notFound("题目"));
        boolean exists = r.getQuestions().stream()
                .anyMatch(iq -> iq.getQuestion().getId().equals(questionId));
        if (exists) throw new ApiException("该题已经在这份面试记录里了");
        r.getQuestions().add(new InterviewQuestion(r, q, note, r.getQuestions().size()));
        return recordRepository.save(r);
    }

    @Transactional
    public InterviewRecord detachQuestion(Long recordId, Long linkId) {
        InterviewRecord r = get(recordId);
        r.getQuestions().removeIf(iq -> iq.getId() != null && iq.getId().equals(linkId));
        return recordRepository.save(r);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listByQuestion(Long questionId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (InterviewQuestion iq : linkRepository.findByQuestionId(questionId)) {
            InterviewRecord r = iq.getRecord();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("recordId", r.getId());
            map.put("company", r.getCompany());
            map.put("position", r.getPosition());
            map.put("round", r.getRound());
            map.put("interviewDate", r.getInterviewDate());
            map.put("result", r.getResult());
            map.put("note", iq.getNote());
            result.add(map);
        }
        return result;
    }
}
