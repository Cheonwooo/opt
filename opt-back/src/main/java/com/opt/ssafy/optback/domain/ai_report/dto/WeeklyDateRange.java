package com.opt.ssafy.optback.domain.ai_report.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeeklyDateRange {
    
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int year;
    private final int month;
    private final int weekNumber;
}
