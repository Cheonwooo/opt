package com.opt.ssafy.optback.domain.license.api;

import com.opt.ssafy.optback.domain.license.application.BusinessLicenseService;
import com.opt.ssafy.optback.global.dto.SuccessResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/license")
public class BusinessLicenseController {

    private final BusinessLicenseService businessLicenseService;

    @PostMapping("/business")
    public ResponseEntity<SuccessResponse> registerBusinessLicense(@RequestPart(name = "image") MultipartFile image)
            throws IOException {
        businessLicenseService.registerBusinessLicense(image);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("잠시만 기다리세용")
                .build());
    }
}
