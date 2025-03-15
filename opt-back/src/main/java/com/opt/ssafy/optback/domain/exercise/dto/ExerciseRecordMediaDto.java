package com.opt.ssafy.optback.domain.exercise.dto;

import com.opt.ssafy.optback.domain.exercise.entity.ExerciseRecordMedia;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExerciseRecordMediaDto {
    private Integer id;
    private String type;
    private String path;
    private LocalDateTime createdAt;

    public static ExerciseRecordMediaDto from(ExerciseRecordMedia exerciseRecordMedia) {
        return ExerciseRecordMediaDto.builder()
                .id(exerciseRecordMedia.getId())
                .type(exerciseRecordMedia.getMediaType())
                .path(exerciseRecordMedia.getMediaPath())
                .createdAt(exerciseRecordMedia.getCreatedAt())
                .build();

    }
}
