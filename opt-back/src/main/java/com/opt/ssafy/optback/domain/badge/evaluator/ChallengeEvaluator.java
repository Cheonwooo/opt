package com.opt.ssafy.optback.domain.badge.evaluator;

import com.opt.ssafy.optback.domain.badge.dto.ActivityType;
import com.opt.ssafy.optback.domain.challenge.repository.ChallengeRecordRepository;
import com.opt.ssafy.optback.domain.exercise.repository.ExerciseRecordRepository;
import com.opt.ssafy.optback.domain.member.entity.Member;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeEvaluator implements BadgeEvaluator {
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final ChallengeRecordRepository challengeRecordRepository;

    @Override
    public boolean evaluate(Member member, Map<String, Object> condition) {
        int challengeClear = Integer.parseInt(condition.get("challengeClear").toString());
        int memberClear = challengeRecordRepository.countByMemberIdAndIsPassedTrue(member.getId());
        if (challengeClear < memberClear) {
            return true;
        }
        return false;
    }

    @Override
    public ActivityType getType() {
        return ActivityType.CHALLENGE;
    }
}
