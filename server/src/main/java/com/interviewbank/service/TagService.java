package com.interviewbank.service;

import com.interviewbank.common.ApiException;
import com.interviewbank.domain.Question;
import com.interviewbank.repo.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TagService {

    private final QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : questionRepository.countByTag()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", row[0]);
            map.put("count", ((Number) row[1]).longValue());
            result.add(map);
        }
        return result;
    }

    @Transactional
    public int rename(String from, String to) {
        if (from == null || from.isBlank() || to == null || to.isBlank()) {
            throw new ApiException("标签名不能为空");
        }
        String target = to.trim();
        if (target.length() > 40) throw new ApiException("标签最长 40 个字符");
        List<Question> questions = questionRepository.findByTag(from.trim());
        for (Question q : questions) {
            q.getTags().remove(from.trim());
            q.getTags().add(target);
        }
        questionRepository.saveAll(questions);
        return questions.size();
    }

    @Transactional
    public int delete(String name) {
        List<Question> questions = questionRepository.findByTag(name);
        questions.forEach(q -> q.getTags().remove(name));
        questionRepository.saveAll(questions);
        return questions.size();
    }
}
