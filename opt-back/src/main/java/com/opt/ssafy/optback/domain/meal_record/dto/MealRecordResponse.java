package com.opt.ssafy.optback.domain.meal_record.dto;

import com.opt.ssafy.optback.domain.meal_record.entity.MealRecord;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MealRecordResponse {

    private LocalDate createdDate;
    private int calorie;
    private float protein;
    private float carbs;
    private float fat;
    private String imagePath;
    private String type;

    public MealRecordResponse(MealRecord mealRecord) {
        this.createdDate = mealRecord.getCreatedDate();
        this.calorie = mealRecord.getCalorie();
        this.protein = mealRecord.getProtein();
        this.carbs = mealRecord.getCarb();
        this.fat = mealRecord.getFat();
        this.imagePath = mealRecord.getImagePath();
        this.type = mealRecord.getType();
    }

}
