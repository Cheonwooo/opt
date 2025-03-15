package com.opt.ssafy.optback.domain.member.application;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class KeywordExtractionService {

    private final RestTemplate restTemplate;

    public KeywordExtractionService(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(100))  // 연결 타임아웃 (기본값: 5초)
                .readTimeout(Duration.ofSeconds(100))     // 응답 타임아웃 (기본값: 5초)
                .build();
    }


    @SuppressWarnings("unchecked")
    public List<String> extractKeywords(String text) {
        String url = "http://fastapi:5000/extract_keywords";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", text);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (List<String>) response.getBody().get("keywords");
        }
        return Collections.emptyList();
    }
}
