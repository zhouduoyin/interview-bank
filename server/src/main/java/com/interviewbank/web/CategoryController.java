package com.interviewbank.web;

import com.interviewbank.domain.Category;
import com.interviewbank.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<Map<String, Object>> list() {
        return categoryService.listWithCounts();
    }

    @PostMapping
    public Category create(@RequestBody Map<String, Object> body) {
        return categoryService.create(body);
    }

    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return categoryService.update(id, body);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Map.of("ok", true);
    }
}
