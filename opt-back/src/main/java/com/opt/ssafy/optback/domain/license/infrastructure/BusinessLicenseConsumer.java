package com.opt.ssafy.optback.domain.license.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opt.ssafy.optback.domain.license.application.BusinessLicenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessLicenseConsumer {

    private final BusinessLicenseService businessLicenseService;

    @Transactional
    @KafkaListener(topics = "business_license_response", groupId = "spring-group")
    public void consume(String message) {
        log.info("사업자 등록증 응답 데이터: {}", message);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            businessLicenseService.handleBusinessLicenseResponse(jsonNode);
        } catch (Exception e) {
            log.error("Error parsing JSON: {}", e.getMessage());
        }
    }

}
