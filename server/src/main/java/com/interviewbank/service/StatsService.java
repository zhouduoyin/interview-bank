package com.interviewbank.service;

import com.interviewbank.domain.*;
import com.interviewbank.repo.InterviewRecordRepository;
import com.interviewbank.repo.QuestionRepository;
import com.interviewbank.repo.ReviewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final QuestionRepository questionRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final InterviewRecordRepository interviewRecordRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> dashboard() {
        List<Question> all = questionRepository.findAll();
        Instant since = Instant.now().minus(400, ChronoUnit.DAYS);
        List<ReviewLog> logs = reviewLogRepository.findByCreatedAtAfterOrderByCreatedAtAsc(since);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("overview", overview(all, logs));
        map.put("heatmap", heatmap(logs, 119));
        map.put("categories", categoryStats(all));
        map.put("difficulties", difficultyStats(all));
        map.put("tags", tagStats(all, 14));
        map.put("trend", trend(logs, 14));
        map.put("recent", recentQuestions(all, 5));
        map.put("masteryByCategory", masteryByCategory(all));
        map.put("upcoming", upcomingInterviews());
        return map;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> insights() {
        List<Question> all = questionRepository.findAll();
        List<ReviewLog> logs = reviewLogRepository.findByCreatedAtAfterOrderByCreatedAtAsc(
                Instant.now().minus(400, ChronoUnit.DAYS));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("categories", categoryStats(all));
        map.put("difficulties", difficultyStats(all));
        map.put("tags", tagStats(all, 30));
        map.put("trend", trend(logs, 30));
        map.put("masteryByCategory", masteryByCategory(all));
        map.put("overview", overview(all, logs));
        return map;
    }

    private Map<String, Object> overview(List<Question> all, List<ReviewLog> logs) {
        Instant now = Instant.now();
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        long total = all.size();
        long mastered = all.stream().filter(q -> q.getMastery() == Mastery.MASTERED).count();
        long learning = all.stream().filter(q -> q.getMastery() == Mastery.LEARNING).count();
        long fresh = all.stream().filter(q -> q.getMastery() == Mastery.NEW).count();
        long due = all.stream()
                .filter(q -> q.getNextReviewAt() != null && !q.getNextReviewAt().isAfter(now))
                .count();
        long starred = all.stream().filter(Question::isStarred).count();
        long newToday = all.stream()
                .filter(q -> q.getCreatedAt() != null && q.getCreatedAt().atZone(zone).toLocalDate().equals(today))
                .count();
        long monthNew = all.stream()
                .filter(q -> q.getCreatedAt() != null &&
                        q.getCreatedAt().atZone(zone).toLocalDate().isAfter(today.withDayOfMonth(1).minusDays(1)))
                .count();
        long todayReviews = logs.stream()
                .filter(l -> l.getCreatedAt().atZone(zone).toLocalDate().equals(today))
                .count();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalQuestions", total);
        map.put("mastered", mastered);
        map.put("learning", learning);
        map.put("newCount", fresh);
        map.put("due", due);
        map.put("starred", starred);
        map.put("newToday", newToday);
        map.put("monthNew", monthNew);
        map.put("totalReviews", reviewLogRepository.count());
        map.put("todayReviews", todayReviews);
        map.put("streakDays", streak(logs, today));
        map.put("interviewCount", interviewRecordRepository.count());
        map.put("masteryRate", total == 0 ? 0.0 : Math.round(mastered * 1000.0 / total) / 10.0);
        map.put("categoryCount", (int) all.stream()
                .map(q -> q.getCategory() == null ? 0L : q.getCategory().getId()).distinct().count());
        map.put("tagCount", all.stream().flatMap(q -> q.getTags().stream()).distinct().count());
        return map;
    }

    private int streak(List<ReviewLog> logs, LocalDate today) {
        Set<LocalDate> days = new HashSet<>();
        ZoneId zone = ZoneId.systemDefault();
        logs.forEach(l -> days.add(l.getCreatedAt().atZone(zone).toLocalDate()));
        LocalDate cursor = days.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private List<Map<String, Object>> heatmap(List<ReviewLog> logs, int days) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate end = LocalDate.now(zone);
        LocalDate start = end.minusDays(days - 1L);
        Map<LocalDate, Integer> counts = new HashMap<>();
        logs.forEach(l -> counts.merge(l.getCreatedAt().atZone(zone).toLocalDate(), 1, Integer::sum));
        List<Map<String, Object>> cells = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            Map<String, Object> cell = new LinkedHashMap<>();
            cell.put("date", d.toString());
            cell.put("count", counts.getOrDefault(d, 0));
            cells.add(cell);
        }
        return cells;
    }

    private List<Map<String, Object>> categoryStats(List<Question> all) {
        Map<Long, String> names = new LinkedHashMap<>();
        Map<Long, String> colors = new LinkedHashMap<>();
        Map<Long, int[]> counts = new LinkedHashMap<>();
        int[] unclassified = new int[2];
        for (Question q : all) {
            if (q.getCategory() == null) {
                unclassified[0]++;
                if (q.getMastery() == Mastery.MASTERED) unclassified[1]++;
                continue;
            }
            Long id = q.getCategory().getId();
            names.putIfAbsent(id, q.getCategory().getName());
            colors.putIfAbsent(id, q.getCategory().getColor());
            int[] c = counts.computeIfAbsent(id, k -> new int[2]);
            c[0]++;
            if (q.getMastery() == Mastery.MASTERED) c[1]++;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        counts.forEach((id, c) -> result.add(categoryEntry(id, names.get(id), colors.get(id), c[0], c[1])));
        if (unclassified[0] > 0) {
            result.add(categoryEntry(null, "未分类", "#94a3b8", unclassified[0], unclassified[1]));
        }
        result.sort((a, b) -> Long.compare(((Number) b.get("count")).longValue(),
                ((Number) a.get("count")).longValue()));
        return result;
    }

    private Map<String, Object> categoryEntry(Long id, String name, String color, int count, int mastered) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("color", color);
        m.put("count", count);
        m.put("mastered", mastered);
        return m;
    }

    private List<Map<String, Object>> difficultyStats(List<Question> all) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Difficulty d : Difficulty.values()) {
            long count = all.stream().filter(q -> q.getDifficulty() == d).count();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("difficulty", d.name());
            m.put("count", count);
            result.add(m);
        }
        return result;
    }

    private List<Map<String, Object>> tagStats(List<Question> all, int limit) {
        Map<String, Integer> counts = new HashMap<>();
        for (Question q : all) q.getTags().forEach(t -> counts.merge(t, 1, Integer::sum));
        return counts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                }).toList();
    }

    private List<Map<String, Object>> trend(List<ReviewLog> logs, int days) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate end = LocalDate.now(zone);
        Map<LocalDate, Integer> counts = new HashMap<>();
        logs.forEach(l -> counts.merge(l.getCreatedAt().atZone(zone).toLocalDate(), 1, Integer::sum));
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = end.minusDays(i);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", d.toString());
            m.put("count", counts.getOrDefault(d, 0));
            result.add(m);
        }
        return result;
    }

    private List<Map<String, Object>> recentQuestions(List<Question> all, int limit) {
        return all.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(limit)
                .map(q -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", q.getId());
                    m.put("title", q.getTitle());
                    m.put("createdAt", q.getCreatedAt());
                    m.put("mastery", q.getMastery());
                    m.put("difficulty", q.getDifficulty());
                    m.put("categoryName", q.getCategory() == null ? "未分类" : q.getCategory().getName());
                    m.put("categoryColor", q.getCategory() == null ? "#94a3b8" : q.getCategory().getColor());
                    return m;
                }).toList();
    }

    private List<Map<String, Object>> masteryByCategory(List<Question> all) {
        Map<String, int[]> map = new LinkedHashMap<>();
        for (Question q : all) {
            String name = q.getCategory() == null ? "未分类" : q.getCategory().getName();
            int[] c = map.computeIfAbsent(name, k -> new int[3]);
            Mastery mastery = q.getMastery() == null ? Mastery.NEW : q.getMastery();
            c[mastery.ordinal()]++;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        map.forEach((name, c) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", name);
            m.put("newCount", c[0]);
            m.put("learning", c[1]);
            m.put("mastered", c[2]);
            result.add(m);
        });
        return result;
    }

    private List<Map<String, Object>> upcomingInterviews() {
        return interviewRecordRepository.findTop5ByOrderByInterviewDateDescIdDesc().stream()
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", r.getId());
                    m.put("company", r.getCompany());
                    m.put("position", r.getPosition());
                    m.put("round", r.getRound());
                    m.put("interviewDate", r.getInterviewDate());
                    m.put("result", r.getResult());
                    return m;
                }).toList();
    }
}
