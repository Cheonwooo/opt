package com.opt.ssafy.optback.domain.certificate.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CertificateDto {
    private String status;
    private String certNumber;
    private String name;
    private String level;
    private Integer id;
    private String path;
    private String newPath;
}
