package com.opt.ssafy.optback.domain.certificate.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opt.ssafy.optback.domain.certificate.application.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateConsumer {

    private final CertificateService certificateService;

    @KafkaListener(topics = "certificate_response")
    public void consume(String message) {
        log.info("자격증 인증 응답 데이터: {}", message);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            certificateService.handleCertificateResponse(jsonNode);
        } catch (Exception e) {
            log.error("Error parsing JSON: {}", e.getMessage());
        }
    }

}
