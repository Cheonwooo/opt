package com.opt.ssafy.optback.domain.ai_report.repository;

import com.opt.ssafy.optback.domain.ai_report.entity.AiReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiReportRepository extends JpaRepository<AiReport, Integer> {

    Optional<AiReport> findByMemberIdAndYearAndMonthAndWeekNumber(int memberId, int year, int month, int weekNumber);
}
