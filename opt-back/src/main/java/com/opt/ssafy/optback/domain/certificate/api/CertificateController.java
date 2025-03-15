package com.opt.ssafy.optback.domain.certificate.api;

import com.opt.ssafy.optback.domain.certificate.application.CertificateService;
import com.opt.ssafy.optback.global.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/certificate")
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping
    public ResponseEntity<SuccessResponse> registerCertificate(@RequestPart MultipartFile image) {
        certificateService.registerCertificate(image);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("잠시만 기다려주세용")
                .build());
    }

    @GetMapping
    public ResponseEntity<?> getCertificateByTrainer(@RequestParam Integer trainerId) {
        return ResponseEntity.ok(certificateService.getTrainerCertificate(trainerId));
    }
}
