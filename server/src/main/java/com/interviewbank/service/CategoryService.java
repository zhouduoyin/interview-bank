package com.interviewbank.service;

import com.interviewbank.common.ApiException;
import com.interviewbank.domain.Category;
import com.interviewbank.domain.Question;
import com.interviewbank.repo.CategoryRepository;
import com.interviewbank.repo.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public Category resolve(Long id) {
        if (id == null) return null;
        return categoryRepository.findById(id).orElseThrow(() -> ApiException.notFound("分类"));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listWithCounts() {
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAscIdAsc();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Category c : categories) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getName());
            map.put("color", c.getColor());
            map.put("description", c.getDescription());
            map.put("sortOrder", c.getSortOrder());
            map.put("demo", c.isDemo());
            map.put("questionCount", questionRepository.countByCategoryId(c.getId()));
            result.add(map);
        }
        return result;
    }

    @Transactional
    public Category create(Map<String, Object> body) {
        String name = string(body.get("name"));
        if (name == null) throw new ApiException("分类名称不能为空");
        if (categoryRepository.existsByNameIgnoreCase(name)) throw new ApiException("分类「" + name + "」已存在");
        Category c = new Category();
        c.setName(name);
        c.setColor(stringOr(body.get("color"), "#a2762a"));
        c.setDescription(string(body.get("description")));
        c.setSortOrder(intOr(body.get("sortOrder"), 0));
        c.setCreatedAt(Instant.now());
        return categoryRepository.save(c);
    }

    @Transactional
    public Category update(Long id, Map<String, Object> body) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> ApiException.notFound("分类"));
        String name = string(body.get("name"));
        if (name != null && !name.equals(c.getName())) {
            if (categoryRepository.existsByNameIgnoreCase(name)) throw new ApiException("分类「" + name + "」已存在");
            c.setName(name);
        }
        if (body.containsKey("color")) c.setColor(stringOr(body.get("color"), c.getColor()));
        if (body.containsKey("description")) c.setDescription(string(body.get("description")));
        if (body.containsKey("sortOrder")) c.setSortOrder(intOr(body.get("sortOrder"), c.getSortOrder()));
        return categoryRepository.save(c);
    }

    @Transactional
    public void delete(Long id) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> ApiException.notFound("分类"));
        questionRepository.clearCategory(id);
        categoryRepository.delete(c);
    }

    private String string(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private String stringOr(Object o, String fallback) {
        String s = string(o);
        return s == null ? fallback : s;
    }

    private int intOr(Object o, int fallback) {
        try {
            return o == null ? fallback : Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
