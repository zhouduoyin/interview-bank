package com.interviewbank.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@ConditionalOnProperty(name = "app.spa.enabled", havingValue = "true", matchIfMissing = true)
public class SpaController {

    @GetMapping({"/", "/questions", "/questions/**", "/review", "/interviews", "/interviews/**",
            "/insights", "/data", "/styleguide"})
    public String index() {
        return "forward:/index.html";
    }
}
