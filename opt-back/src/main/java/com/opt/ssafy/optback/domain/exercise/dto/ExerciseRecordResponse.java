package com.opt.ssafy.optback.domain.exercise.dto;

import com.opt.ssafy.optback.domain.exercise.entity.Exercise;
import com.opt.ssafy.optback.domain.exercise.entity.ExerciseRecord;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExerciseRecordResponse {
    private Integer id;
    private Integer exerciseId;
    private String exerciseName;
    private String exerciseImage;
    private List<ExerciseRecordMediaDto> medias;
    private Integer sets;
    private Integer rep;
    private Integer weight;
    private Integer duration;
    private Integer distance;

    public static ExerciseRecordResponse from(ExerciseRecord exerciseRecord) {
        Exercise exercise = exerciseRecord.getExercise();

        return ExerciseRecordResponse.builder()
                .id(exerciseRecord.getId())
                .exerciseId(exercise.getId())
                .exerciseName(exercise.getName())
                .exerciseImage(exercise.getGifPath())
                .sets(exerciseRecord.getSets())
                .rep(exerciseRecord.getRep())
                .weight(exerciseRecord.getWeight())
                .duration(exerciseRecord.getDuration())
                .distance(exerciseRecord.getDistance())
                .medias(exerciseRecord.getMedias().stream().map(ExerciseRecordMediaDto::from).toList())
                .build();
    }
}
