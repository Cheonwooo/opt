package com.opt.ssafy.optback.domain.challenge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
public class CreateChallengeRequest {
    private String type;
    private String title;
    private String description;
    private String reward;
    private int template_id;
    private int host_id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private String imagePath;
    private String status;
    private int max_participants;
    private int frequency;
    private String exercise_type;
    private Integer exercise_count;
    private Integer exercise_distance;
    private Integer exercise_duration;
    private MultipartFile image;
}
