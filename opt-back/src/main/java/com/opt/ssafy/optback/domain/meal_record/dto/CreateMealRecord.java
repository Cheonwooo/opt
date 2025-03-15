package com.opt.ssafy.optback.domain.meal_record.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class CreateMealRecord {

    private LocalDate createdDate;
    private String type;
    private MultipartFile image;
}
