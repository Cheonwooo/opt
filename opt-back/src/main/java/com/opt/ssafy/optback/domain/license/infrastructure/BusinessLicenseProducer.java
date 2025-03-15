package com.opt.ssafy.optback.domain.license.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opt.ssafy.optback.domain.license.exception.BusinessLicenseMessagingException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessLicenseProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendBusinessLicenseMessage(String path, Integer memberId) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("path", path);
            message.put("id", memberId);
            String jsonMessage = objectMapper.writeValueAsString(message);
            kafkaTemplate.send("business_license_request", jsonMessage);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new BusinessLicenseMessagingException("처리 중 오류 발생 : " + e.getMessage());
        }
    }
}
