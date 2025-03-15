package com.opt.ssafy.optback.domain.exercise.application;

import com.opt.ssafy.optback.domain.exercise.entity.QExercise;
import com.querydsl.core.BooleanBuilder;

public class ExerciseQueryHelper {

    public static void addBodyPartFilter(String bodyPart, QExercise exercise, BooleanBuilder filterBuilder) {
        BooleanBuilder bodyPartBuilder = new BooleanBuilder();
        bodyPartBuilder.or(exercise.bodyPart.eq(bodyPart));
        filterBuilder.and(bodyPartBuilder);
    }

}
