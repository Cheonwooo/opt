package com.opt.ssafy.optback.domain.meal_record.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlyRecordResponse {
    private List<LocalDate> exerciseDates;
    private List<LocalDate> mealDates;
}
