package com.interviewbank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeedRunner implements CommandLineRunner {

    private final SeedService seedService;

    @Override
    public void run(String... args) {
        try {
            seedService.seedIfEmpty();
            log.info("Interview Bank 数据初始化检查完成");
        } catch (Exception e) {
            log.warn("示例数据初始化失败（可忽略，不影响使用）：{}", e.getMessage());
        }
    }
}
