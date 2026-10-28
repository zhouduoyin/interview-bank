package com.interviewbank.service;

import com.interviewbank.common.ApiException;
import com.interviewbank.common.PageResult;
import com.interviewbank.domain.*;
import com.interviewbank.repo.InterviewQuestionRepository;
import com.interviewbank.repo.QuestionRepository;
import com.interviewbank.repo.ReviewLogRepository;
import com.interviewbank.request.BatchRequest;
import com.interviewbank.request.QuestionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public PageResult<Question> search(String keyword, Long categoryId, Difficulty difficulty, Mastery mastery,
                                       String tag, Boolean starred, String sort, int page, int size) {
        String kw = (keyword == null || keyword.isBlank()) ? null : "%" + keyword.trim().toLowerCase() + "%";
        String tagFilter = (tag == null || tag.isBlank()) ? null : tag.trim();
        Sort sorting = switch (sort == null ? "" : sort) {
            case "created" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "reviews" -> Sort.by(Sort.Direction.DESC, "reviewCount").and(Sort.by(Sort.Direction.DESC, "updatedAt"));
            case "due" -> Sort.by(Sort.Direction.ASC, "nextReviewAt");
            case "difficulty" -> Sort.by(Sort.Direction.DESC, "difficulty");
            default -> Sort.by(Sort.Direction.DESC, "updatedAt");
        };
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sorting);
        Page<Question> result = questionRepository.search(kw, categoryId, difficulty, mastery, tagFilter, starred, pageable);
        return PageResult.of(result);
    }

    @Transactional(readOnly = true)
    public Question get(Long id) {
        return questionRepository.findById(id).orElseThrow(() -> ApiException.notFound("题目"));
    }

    @Transactional
    public Question create(QuestionRequest req) {
        Question q = new Question();
        apply(q, req);
        q.setNextReviewAt(Instant.now());
        q.setCreatedAt(Instant.now());
        return questionRepository.save(q);
    }

    @Transactional
    public Question update(Long id, QuestionRequest req) {
        Question q = get(id);
        apply(q, req);
        return questionRepository.save(q);
    }

    private void apply(Question q, QuestionRequest req) {
        q.setTitle(req.title().trim());
        q.setContent(req.content());
        q.setAnswer(req.answer());
        q.setNote(req.note());
        q.setDifficulty(req.difficulty() == null ? Difficulty.MEDIUM : req.difficulty());
        if (req.mastery() != null) {
            q.setMastery(req.mastery());
        }
        q.setSourceCompany(trimToNull(req.sourceCompany()));
        q.setSourcePosition(trimToNull(req.sourcePosition()));
        if (req.starred() != null) {
            q.setStarred(req.starred());
        }
        q.setCategory(categoryService.resolve(req.categoryId()));
        Set<String> tags = new LinkedHashSet<>();
        if (req.tags() != null) {
            req.tags().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(t -> !t.isEmpty() && t.length() <= 40)
                    .forEach(tags::add);
        }
        q.setTags(tags);
    }

    private String trimToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    @Transactional
    public void delete(Long id) {
        Question q = get(id);
        reviewLogRepository.deleteByQuestionId(id);
        interviewQuestionRepository.deleteByQuestionId(id);
        questionRepository.delete(q);
    }

    @Transactional
    public Question toggleStar(Long id) {
        Question q = get(id);
        q.setStarred(!q.isStarred());
        return questionRepository.save(q);
    }

    @Transactional
    public Question setMastery(Long id, Mastery mastery) {
        Question q = get(id);
        q.setMastery(mastery);
        if (mastery == Mastery.MASTERED && q.getNextReviewAt() != null
                && q.getNextReviewAt().isBefore(Instant.now())) {
            q.setNextReviewAt(Instant.now().plus(7, ChronoUnit.DAYS));
            q.setIntervalDays(Math.max(q.getIntervalDays(), 7));
        }
        return questionRepository.save(q);
    }

    @Transactional
    public Question review(Long id, Rating rating) {
        Question q = get(id);
        Instant now = Instant.now();
        int prev = Math.max(q.getIntervalDays(), 0);
        double ease = q.getEaseFactor() <= 0 ? 2.5 : q.getEaseFactor();
        int next;
        switch (rating) {
            case FORGOT -> {
                ease = Math.max(1.3, ease - 0.25);
                next = 1;
                q.setMastery(Mastery.LEARNING);
            }
            case HARD -> {
                ease = Math.max(1.3, ease - 0.15);
                next = prev <= 0 ? 1 : Math.max(1, (int) Math.round(prev * 1.2));
            }
            case GOOD -> next = prev <= 0 ? 1 : (int) Math.round(prev * ease);
            default -> {
                ease = Math.min(3.2, ease + 0.15);
                next = prev <= 0 ? 3 : (int) Math.round(prev * ease * 1.3);
            }
        }
        next = Math.min(Math.max(next, 1), 365);
        int count = q.getReviewCount() + 1;
        q.setIntervalDays(next);
        q.setEaseFactor(Math.round(ease * 100) / 100.0);
        q.setReviewCount(count);
        q.setLastReviewedAt(now);
        q.setNextReviewAt(now.plus(next, ChronoUnit.DAYS));
        if (rating != Rating.FORGOT && (next >= 7 || count >= 3)) {
            q.setMastery(Mastery.MASTERED);
        } else {
            q.setMastery(Mastery.LEARNING);
        }
        questionRepository.save(q);

        ReviewLog log = new ReviewLog();
        log.setQuestion(q);
        log.setRating(rating);
        log.setPrevInterval(prev);
        log.setIntervalDays(next);
        log.setCreatedAt(now);
        reviewLogRepository.save(log);
        return q;
    }

    @Transactional(readOnly = true)
    public List<ReviewLog> reviewLogs(Long id) {
        get(id);
        return reviewLogRepository.findByQuestionIdOrderByCreatedAtDesc(id);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> reviewQueue(int limit) {
        Instant now = Instant.now();
        List<Question> due = questionRepository.findByNextReviewAtBeforeOrderByNextReviewAtAsc(
                now, PageRequest.of(0, Math.min(Math.max(limit, 1), 100)));
        long dueCount = questionRepository.countByNextReviewAtBefore(now);
        long newCount = questionRepository.countByNextReviewAtBeforeAndReviewCount(now, 0);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("items", due);
        map.put("dueCount", dueCount);
        map.put("newCount", newCount);
        return map;
    }

    @Transactional
    public Map<String, Object> batch(BatchRequest req) {
        if (req.ids() == null || req.ids().isEmpty()) {
            throw new ApiException("请先选择题目");
        }
        List<Question> questions = questionRepository.findAllById(req.ids());
        String action = req.action() == null ? "" : req.action();
        switch (action) {
            case "delete" -> {
                for (Question q : questions) {
                    reviewLogRepository.deleteByQuestionId(q.getId());
                    interviewQuestionRepository.deleteByQuestionId(q.getId());
                }
                questionRepository.deleteAll(questions);
            }
            case "mastery" -> {
                if (req.mastery() == null) throw new ApiException("缺少掌握程度");
                questions.forEach(q -> q.setMastery(req.mastery()));
                questionRepository.saveAll(questions);
            }
            case "category" -> {
                Category category = categoryService.resolve(req.categoryId());
                questions.forEach(q -> q.setCategory(category));
                questionRepository.saveAll(questions);
            }
            case "star" -> {
                questions.forEach(q -> q.setStarred(true));
                questionRepository.saveAll(questions);
            }
            default -> throw new ApiException("不支持的批量操作：" + action);
        }
        return Map.of("affected", questions.size());
    }

    @Transactional(readOnly = true)
    public List<Question> recent(int limit) {
        return questionRepository.findAll(PageRequest.of(0, limit,
                Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }
}
