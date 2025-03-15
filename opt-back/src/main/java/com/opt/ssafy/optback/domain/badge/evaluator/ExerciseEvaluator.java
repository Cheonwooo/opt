package com.opt.ssafy.optback.domain.badge.evaluator;

import com.opt.ssafy.optback.domain.badge.dto.ActivityType;
import com.opt.ssafy.optback.domain.exercise.repository.ExerciseRecordRepository;
import com.opt.ssafy.optback.domain.member.entity.Member;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExerciseEvaluator implements BadgeEvaluator {

    private final ExerciseRecordRepository exerciseRecordRepository;

    @Override
    public boolean evaluate(Member member, Map<String, Object> condition) {
        if (!condition.containsKey("exerciseId")) {
            log.error("❌ exerciseId 없음");
            return false;
        }

        int exerciseId = Integer.parseInt(condition.get("exerciseId").toString());

        if (condition.containsKey("totalDistance")) {
            int requiredDistance = Integer.parseInt(condition.get("totalDistance").toString());
            int memberDistance = exerciseRecordRepository.getTotalDistanceByMemberAndExercise(member.getId(),
                    exerciseId);
            if (memberDistance >= requiredDistance) {
                return true;
            }
        }

        if (condition.containsKey("totalWeight")) {
            int requiredWeight = Integer.parseInt(condition.get("totalWeight").toString());
            int memberWeight = exerciseRecordRepository.getTotalWeightByMemberAndExercise(member.getId(), exerciseId);
            if (memberWeight >= requiredWeight) {
                return true;
            }
        }

        if (condition.containsKey("exerciseClear")) {
            int requiredClearCount = Integer.parseInt(condition.get("exerciseClear").toString());
            int memberClearCount = exerciseRecordRepository.getExerciseCompletionCount(member.getId(), exerciseId);
            if (memberClearCount >= requiredClearCount) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ActivityType getType() {
        return ActivityType.EXERCISE;
    }
}
