package com.opt.ssafy.optback.domain.meal_record.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealRecordRequest {

    private LocalDate createdDate;
    private String type;
}
