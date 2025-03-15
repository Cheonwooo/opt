package com.opt.ssafy.optback.domain.meal_record.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealNutritionDto {

    private int calorie;
    private float protein;
    private float carb;
    private float fat;
}
