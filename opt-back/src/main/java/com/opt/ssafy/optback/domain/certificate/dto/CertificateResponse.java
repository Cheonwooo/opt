package com.opt.ssafy.optback.domain.certificate.dto;

import com.opt.ssafy.optback.domain.certificate.entity.Certificate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CertificateResponse {

    private String name;
    private Boolean isVerified;
    private String imagePath;

    public static CertificateResponse from(Certificate certificate) {
        return CertificateResponse.builder()
                .name(certificate.getName())
                .isVerified(certificate.getIsVerified())
                .imagePath(certificate.getPath())
                .build();
    }

}
