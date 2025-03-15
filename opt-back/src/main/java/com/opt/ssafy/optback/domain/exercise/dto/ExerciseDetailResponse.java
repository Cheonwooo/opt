package com.opt.ssafy.optback.domain.exercise.dto;

import com.opt.ssafy.optback.domain.exercise.entity.Exercise;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExerciseDetailResponse {
    private Integer id;
    private String name;
    private String bodyPart;
    private List<ExerciseInstructionDto> instructions;

    public static ExerciseDetailResponse from(Exercise exercise) {
        return ExerciseDetailResponse.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .bodyPart(exercise.getBodyPart())
                .instructions(exercise.getInstructions().stream().map(ExerciseInstructionDto::from).toList())
                .build();
    }
}
