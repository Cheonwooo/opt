package com.opt.ssafy.optback.domain.ai_report.controller;

import com.opt.ssafy.optback.domain.ai_report.service.AiReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai-reports")
@RequiredArgsConstructor
public class AiReportController {

    private final AiReportService aiReportService;

    @GetMapping
    public ResponseEntity<String> getAiReport(@RequestParam int year, @RequestParam int month,
                                              @RequestParam int weekNumber) {
        String content = aiReportService.getAiReportContent(year, month, weekNumber).getContent();
        return ResponseEntity.ok(content);
    }
}

