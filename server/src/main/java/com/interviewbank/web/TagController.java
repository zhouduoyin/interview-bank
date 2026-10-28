package com.interviewbank.web;

import com.interviewbank.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public List<Map<String, Object>> list() {
        return tagService.list();
    }

    @PutMapping("/rename")
    public Map<String, Object> rename(@RequestBody Map<String, String> body) {
        int affected = tagService.rename(body.get("from"), body.get("to"));
        return Map.of("affected", affected);
    }

    @DeleteMapping("/{name}")
    public Map<String, Object> delete(@PathVariable String name) {
        return Map.of("affected", tagService.delete(name));
    }
}
