package com.opt.ssafy.optback.domain.ai_report.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyExerciseSummary {

    private int totalWorkouts;
    private int totalDuration;
    private int totalDistance;
    private Map<String, Long> exerciseFrequency;
}
