package com.opt.ssafy.optback.domain.badge.evaluator;

import com.opt.ssafy.optback.domain.badge.dto.ActivityType;
import com.opt.ssafy.optback.domain.exercise.application.ExerciseRecordService;
import com.opt.ssafy.optback.domain.exercise.dto.ExerciseRecordResponse;
import com.opt.ssafy.optback.domain.exercise.repository.ExerciseRecordRepository;
import com.opt.ssafy.optback.domain.member.entity.Member;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceEvaluator implements BadgeEvaluator {

    private final ExerciseRecordService exerciseRecordService;
    private final ExerciseRecordRepository exerciseRecordRepository;

    // 1일 출석
    @Override
    public boolean evaluate(Member member, Map<String, Object> condition) {
        int targetDays = Integer.parseInt(condition.get("targetDays").toString());
        LocalDate today = LocalDate.now();
        for (int i = 1; i < targetDays; i++) {
            List<ExerciseRecordResponse> list = exerciseRecordService.findExerciseRecordsByDate(today.minusDays(i));
            if (list == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ActivityType getType() {
        return ActivityType.ATTENDANCE;
    }
}
