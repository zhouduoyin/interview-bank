package com.interviewbank.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.interviewbank.service.DataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {

    private final DataService dataService;

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> export() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"interview-bank-" + LocalDate.now() + ".json\"")
                .body(dataService.exportData());
    }

    @GetMapping(value = "/export/markdown", produces = "text/markdown;charset=UTF-8")
    public ResponseEntity<byte[]> exportMarkdown() {
        byte[] bytes = dataService.exportMarkdown().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"interview-bank-" + LocalDate.now() + ".md\"")
                .contentType(new MediaType("text", "markdown", StandardCharsets.UTF_8))
                .body(bytes);
    }

    @PostMapping("/import")
    public Map<String, Object> importData(@RequestBody JsonNode root) {
        return dataService.importData(root);
    }

    @DeleteMapping("/demo")
    public Map<String, Object> clearDemo() {
        return dataService.clearDemo();
    }

    @DeleteMapping("/all")
    public Map<String, Object> clearAll() {
        return dataService.clearAll();
    }
}
