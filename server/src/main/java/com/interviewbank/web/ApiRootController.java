package com.interviewbank.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 前后端分离部署时，后端根路径只返回服务信息，不托管前端页面。
 */
@RestController
@ConditionalOnProperty(name = "app.spa.enabled", havingValue = "false")
public class ApiRootController {

    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("app", "interview-bank");
        info.put("mode", "api-only");
        info.put("api", "/api");
        info.put("message", "Frontend is served separately. Use the Vue / Nginx address.");
        return info;
    }
}
