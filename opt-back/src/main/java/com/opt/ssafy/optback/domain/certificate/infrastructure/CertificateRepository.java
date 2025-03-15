package com.opt.ssafy.optback.domain.certificate.infrastructure;

import com.opt.ssafy.optback.domain.certificate.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Integer> {
}
