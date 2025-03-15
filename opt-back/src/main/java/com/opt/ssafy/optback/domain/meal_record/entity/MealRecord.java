package com.opt.ssafy.optback.domain.meal_record.entity;

import com.opt.ssafy.optback.domain.meal_record.dto.MealNutritionDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meal_record")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "member_id", nullable = false)
    private int memberId;

    @Column(name = "created_Date", nullable = false)
    private LocalDate createdDate;

    @Column(name = "calorie")
    private int calorie;

    @Column(name = "protein")
    private float protein;

    @Column(name = "carb")
    private float carb;

    @Column(name = "fat")
    private float fat;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "type", nullable = false)
    private String type;

    public void setNewRecord(String type) {
        this.type = type;
    }

    public void setNewImage(String imagePath) {
        this.imagePath = imagePath;
        this.setNutrition(new MealNutritionDto(0, 0, 0, 0));
    }

    public void setNutrition(MealNutritionDto nutrition) {
        this.calorie = nutrition.getCalorie();
        this.protein = nutrition.getProtein();
        this.carb = nutrition.getCarb();
        this.fat = nutrition.getFat();
    }
}
