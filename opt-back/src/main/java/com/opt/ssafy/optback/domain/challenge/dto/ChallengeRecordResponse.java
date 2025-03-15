package com.opt.ssafy.optback.domain.challenge.dto;

import com.opt.ssafy.optback.domain.challenge.entity.ChallengeRecord;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeRecordResponse {
    private int challengeId;
    private Integer count;
    private Integer distance;
    private Integer duration;
    private boolean isPassed;
    private Date createdAt;
    private float progress;

    public static ChallengeRecordResponse fromEntity(ChallengeRecord record) {
        float progress = 0.0f;

        Integer count = record.getCount();
        Integer distance = record.getDistance();
        Integer duration = record.getDuration();
        boolean isPassed = record.isPassed();
        Date createdAt = record.getCreatedAt();

        Integer challengeId = (record.getChallenge() != null) ? record.getChallenge().getId() : null;

        if (record.getChallenge() != null && ("NORMAL".equals(record.getChallenge().getType()) || "SURVIVAL".equals(record.getChallenge().getType()))) {
            if (count != null && count>0 && record.getChallenge().getExerciseCount() > 0) {
                progress = Math.round((float) count / record.getChallenge().getExerciseCount() * 100);
            } else if (duration != null && record.getChallenge().getExerciseDuration() != null && record.getChallenge().getExerciseDuration() > 0) {
                progress = Math.round((float) duration / record.getChallenge().getExerciseDuration() * 100);
            } else if (distance != null && record.getChallenge().getExerciseDistance() != null && record.getChallenge().getExerciseDistance() > 0) {
                progress = Math.round((float) distance / record.getChallenge().getExerciseDistance() * 100);
            }
        }
        if(record.getChallenge() != null && "TEAM".equals(record.getChallenge().getType())) {
            progress = record.getChallenge().getProgress() != null ? record.getChallenge().getProgress() : 0.0f;
        }

        return new ChallengeRecordResponse(
                challengeId,
                count,
                distance,
                duration,
                isPassed,
                createdAt,
                progress
        );
    }
}


