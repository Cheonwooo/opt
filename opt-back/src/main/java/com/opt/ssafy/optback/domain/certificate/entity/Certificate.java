package com.opt.ssafy.optback.domain.certificate.entity;

import com.opt.ssafy.optback.domain.certificate.dto.CertificateDto;
import com.opt.ssafy.optback.domain.trainer_detail.entity.TrainerDetail;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "certificate")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Column(name = "path")
    private String path;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private TrainerDetail trainerDetail;

    public static Certificate from(CertificateDto dto, TrainerDetail trainerDetail) {
        return Certificate.builder()
                .name(dto.getLevel())
                .trainerDetail(trainerDetail)
                .isVerified(true)
                .path(dto.getNewPath())
                .build();
    }
}

