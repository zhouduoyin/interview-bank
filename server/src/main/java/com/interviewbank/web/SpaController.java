package com.interviewbank.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({"/", "/questions", "/questions/**", "/review", "/interviews", "/interviews/**",
            "/insights", "/data", "/styleguide"})
    public String index() {
        return "forward:/index.html";
    }
}
