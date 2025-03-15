package com.opt.ssafy.optback.domain.exercise.dto;

import com.opt.ssafy.optback.domain.exercise.entity.ExerciseInstruction;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExerciseInstructionDto {
    private Integer order;
    private String content;

    public static ExerciseInstructionDto from(ExerciseInstruction exerciseInstruction) {
        return ExerciseInstructionDto.builder()
                .order(exerciseInstruction.getOrder())
                .content(exerciseInstruction.getContent())
                .build();
    }
}
