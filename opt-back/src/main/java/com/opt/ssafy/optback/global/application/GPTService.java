package com.opt.ssafy.optback.global.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opt.ssafy.optback.config.ChatGPTConfig;
import com.opt.ssafy.optback.global.dto.GptRequest;
import com.opt.ssafy.optback.global.exception.GPTException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GPTService {

    private final ChatGPTConfig chatGPTConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    public Object requestGPT(String prompt) {
        GptRequest gptRequest = GptRequest.builder()
                .model(model)
                .prompt(prompt)
                .temperature(0.8f)
                .build();

        String gptResponse = sendRequest(gptRequest);
        return parseContent(gptResponse);
    }

    private String sendRequest(GptRequest gptRequest) {
        HttpHeaders headers = chatGPTConfig.headers();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", gptRequest.getModel());
        requestBody.put("messages", List.of(Map.of("role", "user", "content", gptRequest.getPrompt())));
        requestBody.put("temperature", gptRequest.getTemperature());

        String jsonRequestBody;
        try {
            jsonRequestBody = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new GPTException("GPT 요청 변환 실패");
        }

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
        ResponseEntity<String> responseEntity = chatGPTConfig.restTemplate()
                .exchange(openaiApiUrl, HttpMethod.POST, requestEntity, String.class);

        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new GPTException("GPT 호출 실패");
        }

        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseEntity.getBody(), Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");

            if (choices.isEmpty()) {
                throw new GPTException("GPT 응답이 비어 있습니다");
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            throw new GPTException("GPT 응답 변환 실패");
        }
    }

    // GPT 응답 타입에 따라 변환
    private Object parseContent(String content) {
        // JSON형태인지 파악
        try {
            return objectMapper.readValue(content, Map.class);
        } catch (JsonProcessingException e) {
            return content;
        }
    }
}
