package com.opt.ssafy.optback.domain.ai_report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyMealSummary {
    
    private int totalCalories;
    private float totalProtein;
    private float totalCarb;
    private float totalFat;

}
