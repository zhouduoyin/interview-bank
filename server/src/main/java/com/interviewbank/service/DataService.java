package com.interviewbank.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.interviewbank.common.ApiException;
import com.interviewbank.domain.*;
import com.interviewbank.repo.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DataService {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final InterviewRecordRepository recordRepository;
    private final InterviewQuestionRepository linkRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Map<String, Object> exportData() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("app", "interview-bank");
        root.put("version", 1);
        root.put("exportedAt", Instant.now().toString());

        List<Map<String, Object>> categories = new ArrayList<>();
        for (Category c : categoryRepository.findAllByOrderBySortOrderAscIdAsc()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", c.getName());
            m.put("color", c.getColor());
            m.put("description", c.getDescription());
            m.put("sortOrder", c.getSortOrder());
            categories.add(m);
        }
        root.put("categories", categories);

        List<Map<String, Object>> questions = new ArrayList<>();
        for (Question q : questionRepository.findAll()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("title", q.getTitle());
            m.put("content", q.getContent());
            m.put("answer", q.getAnswer());
            m.put("note", q.getNote());
            m.put("categoryName", q.getCategory() == null ? null : q.getCategory().getName());
            m.put("difficulty", q.getDifficulty().name());
            m.put("mastery", q.getMastery().name());
            m.put("tags", q.getTags());
            m.put("sourceCompany", q.getSourceCompany());
            m.put("sourcePosition", q.getSourcePosition());
            m.put("starred", q.isStarred());
            m.put("reviewCount", q.getReviewCount());
            m.put("intervalDays", q.getIntervalDays());
            m.put("easeFactor", q.getEaseFactor());
            m.put("createdAt", q.getCreatedAt() == null ? null : q.getCreatedAt().toString());
            m.put("nextReviewAt", q.getNextReviewAt() == null ? null : q.getNextReviewAt().toString());
            m.put("lastReviewedAt", q.getLastReviewedAt() == null ? null : q.getLastReviewedAt().toString());
            questions.add(m);
        }
        root.put("questions", questions);

        List<Map<String, Object>> interviews = new ArrayList<>();
        for (InterviewRecord r : recordRepository.findAllByOrderByInterviewDateDescIdDesc()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("company", r.getCompany());
            m.put("position", r.getPosition());
            m.put("round", r.getRound());
            m.put("interviewDate", r.getInterviewDate() == null ? null : r.getInterviewDate().toString());
            m.put("result", r.getResult().name());
            m.put("channel", r.getChannel());
            m.put("salaryRange", r.getSalaryRange());
            m.put("summary", r.getSummary());
            m.put("selfRating", r.getSelfRating());
            List<Map<String, Object>> links = new ArrayList<>();
            for (InterviewQuestion iq : r.getQuestions()) {
                Map<String, Object> l = new LinkedHashMap<>();
                l.put("title", iq.getQuestion().getTitle());
                l.put("note", iq.getNote());
                links.add(l);
            }
            m.put("questions", links);
            interviews.add(m);
        }
        root.put("interviews", interviews);
        return root;
    }

    @Transactional(readOnly = true)
    public String exportMarkdown() {
        StringBuilder sb = new StringBuilder();
        sb.append("# 面试题库导出\n\n> 导出时间：").append(LocalDate.now()).append("\n\n");
        for (Category c : categoryRepository.findAllByOrderBySortOrderAscIdAsc()) {
            List<Question> list = questionRepository.findByCategory(c.getId());
            if (list.isEmpty()) continue;
            sb.append("## ").append(c.getName()).append("\n\n");
            for (Question q : list) {
                sb.append("### ").append(q.getTitle()).append("\n\n");
                sb.append("- 难度：").append(label(q.getDifficulty()))
                        .append(" ｜ 掌握：").append(label(q.getMastery()))
                        .append(" ｜ 标签：").append(String.join(", ", q.getTags())).append("\n\n");
                if (q.getContent() != null && !q.getContent().isBlank()) {
                    sb.append("**题目**\n\n").append(q.getContent()).append("\n\n");
                }
                if (q.getAnswer() != null && !q.getAnswer().isBlank()) {
                    sb.append("**参考答案**\n\n").append(q.getAnswer()).append("\n\n");
                }
                sb.append("---\n\n");
            }
        }
        List<Question> uncategorized = questionRepository.findAll().stream()
                .filter(q -> q.getCategory() == null).toList();
        if (!uncategorized.isEmpty()) {
            sb.append("## 未分类\n\n");
            for (Question q : uncategorized) {
                sb.append("### ").append(q.getTitle()).append("\n\n");
                if (q.getContent() != null) sb.append(q.getContent()).append("\n\n");
                if (q.getAnswer() != null) sb.append("**参考答案**\n\n").append(q.getAnswer()).append("\n\n");
                sb.append("---\n\n");
            }
        }
        return sb.toString();
    }

    private String label(Enum<?> e) {
        return switch (e.name()) {
            case "EASY" -> "简单";
            case "MEDIUM" -> "中等";
            case "HARD" -> "困难";
            case "NEW" -> "未掌握";
            case "LEARNING" -> "复习中";
            case "MASTERED" -> "已掌握";
            default -> e.name();
        };
    }

    @Transactional
    public Map<String, Object> importData(JsonNode root) {
        if (root == null || !root.has("questions")) {
            throw new ApiException("导入文件格式不正确：缺少 questions 字段");
        }
        Map<String, Category> categoryByName = new LinkedHashMap<>();
        categoryRepository.findAll().forEach(c -> categoryByName.put(c.getName(), c));
        int categoryCount = 0;
        int createdCategories = 0;

        if (root.has("categories") && root.get("categories").isArray()) {
            for (JsonNode node : root.get("categories")) {
                String name = text(node, "name");
                if (name == null) continue;
                categoryCount++;
                if (!categoryByName.containsKey(name)) {
                    Category c = new Category(name,
                            textOr(node, "color", "#a2762a"),
                            text(node, "description"),
                            node.path("sortOrder").asInt(0));
                    categoryByName.put(name, categoryRepository.save(c));
                    createdCategories++;
                }
            }
        }

        int imported = 0;
        int skipped = 0;
        for (JsonNode node : root.get("questions")) {
            String title = text(node, "title");
            if (title == null) {
                skipped++;
                continue;
            }
            if (questionRepository.findFirstByTitle(title).isPresent()) {
                skipped++;
                continue;
            }
            Question q = new Question();
            q.setTitle(title);
            q.setContent(text(node, "content"));
            q.setAnswer(text(node, "answer"));
            q.setNote(text(node, "note"));
            String categoryName = text(node, "categoryName");
            if (categoryName != null) {
                Category category = categoryByName.get(categoryName);
                if (category == null) {
                    category = categoryRepository.save(new Category(categoryName, "#a2762a", null,
                            categoryByName.size() + 1));
                    categoryByName.put(categoryName, category);
                    createdCategories++;
                }
                q.setCategory(category);
            }
            q.setDifficulty(enumOr(node, "difficulty", Difficulty.class, Difficulty.MEDIUM));
            q.setMastery(enumOr(node, "mastery", Mastery.class, Mastery.NEW));
            if (node.has("tags") && node.get("tags").isArray()) {
                Set<String> tags = new LinkedHashSet<>();
                node.get("tags").forEach(t -> {
                    String v = t.asText("").trim();
                    if (!v.isEmpty()) tags.add(v);
                });
                q.setTags(tags);
            }
            q.setSourceCompany(text(node, "sourceCompany"));
            q.setSourcePosition(text(node, "sourcePosition"));
            q.setStarred(node.path("starred").asBoolean(false));
            q.setReviewCount(node.path("reviewCount").asInt(0));
            q.setIntervalDays(node.path("intervalDays").asInt(0));
            if (node.hasNonNull("easeFactor")) q.setEaseFactor(node.get("easeFactor").asDouble(2.5));
            q.setCreatedAt(instantOr(node, "createdAt", Instant.now()));
            q.setNextReviewAt(instantOr(node, "nextReviewAt", Instant.now()));
            q.setLastReviewedAt(instantOr(node, "lastReviewedAt", null));
            questionRepository.save(q);
            imported++;
        }

        int importedInterviews = 0;
        if (root.has("interviews") && root.get("interviews").isArray()) {
            for (JsonNode node : root.get("interviews")) {
                String company = text(node, "company");
                if (company == null) continue;
                InterviewRecord r = new InterviewRecord();
                r.setCompany(company);
                r.setPosition(text(node, "position"));
                r.setRound(text(node, "round"));
                r.setInterviewDate(dateOr(node, "interviewDate", LocalDate.now()));
                r.setResult(enumOr(node, "result", InterviewResult.class, InterviewResult.PENDING));
                r.setChannel(text(node, "channel"));
                r.setSalaryRange(text(node, "salaryRange"));
                r.setSummary(text(node, "summary"));
                if (node.hasNonNull("selfRating")) r.setSelfRating(node.get("selfRating").asInt());
                if (node.has("questions") && node.get("questions").isArray()) {
                    int index = 0;
                    for (JsonNode link : node.get("questions")) {
                        String qTitle = text(link, "title");
                        if (qTitle == null) continue;
                        Optional<Question> q = questionRepository.findFirstByTitle(qTitle);
                        if (q.isEmpty()) continue;
                        r.getQuestions().add(new InterviewQuestion(r, q.get(), text(link, "note"), index++));
                    }
                }
                recordRepository.save(r);
                importedInterviews++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("categories", createdCategories);
        result.put("questions", imported);
        result.put("interviews", importedInterviews);
        result.put("skipped", skipped);
        result.put("categoryTotal", categoryCount);
        return result;
    }

    @Transactional
    public Map<String, Object> clearDemo() {
        List<Question> demoQuestions = questionRepository.findByDemoTrue();
        for (Question q : demoQuestions) {
            reviewLogRepository.deleteByQuestionId(q.getId());
            linkRepository.deleteByQuestionId(q.getId());
        }
        questionRepository.deleteAll(demoQuestions);

        List<InterviewRecord> demoRecords = recordRepository.findByDemoTrue();
        recordRepository.deleteAll(demoRecords);
        recordRepository.flush();

        int deletedCategories = 0;
        for (Category c : categoryRepository.findByDemoTrue()) {
            if (questionRepository.countByCategoryId(c.getId()) == 0) {
                categoryRepository.delete(c);
                deletedCategories++;
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("questions", demoQuestions.size());
        result.put("interviews", demoRecords.size());
        result.put("categories", deletedCategories);
        return result;
    }

    @Transactional
    public Map<String, Object> clearAll() {
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("questions", questionRepository.count());
        before.put("interviews", recordRepository.count());
        // 原生删除，避免批量 JPQL 绕过集合表/关联表的级联清理导致外键约束失败
        entityManager.createNativeQuery("delete from question_tags").executeUpdate();
        entityManager.createNativeQuery("delete from review_log").executeUpdate();
        entityManager.createNativeQuery("delete from interview_question").executeUpdate();
        entityManager.createNativeQuery("delete from interview_record").executeUpdate();
        entityManager.createNativeQuery("delete from question").executeUpdate();
        entityManager.createNativeQuery("delete from category").executeUpdate();
        return before;
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return null;
        String s = v.asText().trim();
        return s.isEmpty() ? null : s;
    }

    private String textOr(JsonNode node, String field, String fallback) {
        String s = text(node, field);
        return s == null ? fallback : s;
    }

    private Instant instantOr(JsonNode node, String field, Instant fallback) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return fallback;
        try {
            return Instant.parse(v.asText());
        } catch (Exception e) {
            try {
                return LocalDate.parse(v.asText()).atStartOfDay().toInstant(ZoneOffset.UTC);
            } catch (Exception ignored) {
                return fallback;
            }
        }
    }

    private LocalDate dateOr(JsonNode node, String field, LocalDate fallback) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return fallback;
        try {
            return LocalDate.parse(v.asText());
        } catch (Exception e) {
            return fallback;
        }
    }

    private <E extends Enum<E>> E enumOr(JsonNode node, String field, Class<E> type, E fallback) {
        String s = text(node, field);
        if (s == null) return fallback;
        try {
            return Enum.valueOf(type, s.toUpperCase());
        } catch (Exception e) {
            return fallback;
        }
    }
}
