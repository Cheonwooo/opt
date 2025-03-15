package com.opt.ssafy.optback.domain.challenge.dto;

import com.opt.ssafy.optback.domain.challenge.entity.Challenge;
import com.opt.ssafy.optback.domain.member.entity.Member;
import java.time.LocalDate;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChallengeResponse {
    private int id;
    private String type;
    private String title;
    private String description;
    private String reward;
    private Integer templateId;
    private String winnerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Date createdAt;
    private Integer currentParticipants;
    private int maxParticipants;
    private int frequency;
    private Float progress;
    private String imagePath;
    private String exerciseType;
    private Integer exerciseCount;
    private Integer hostId;
    private String hostImagePath;
    private String hostNickname;
    private String hostRealName;
    private Integer exerciseDuration;
    private Integer exerciseDistance;

    public static ChallengeResponse from(Challenge challenge, Member host, String winnerNickname) {
        return ChallengeResponse.builder()
                .id(challenge.getId())
                .type(challenge.getType())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .reward(challenge.getReward())
                .templateId(challenge.getTemplateId())
                .winnerName(winnerNickname)
                .hostNickname(host.getNickname())
                .hostRealName(host.getName())
                .hostImagePath(host.getImagePath())
                .startDate(challenge.getStartDate())
                .hostId(host.getId())
                .endDate(challenge.getEndDate())
                .status(challenge.getStatus())
                .createdAt(challenge.getCreatedAt())
                .currentParticipants(challenge.getCurrentParticipants())
                .maxParticipants(challenge.getMaxParticipants())
                .frequency(challenge.getFrequency())
                .progress(challenge.getProgress())
                .imagePath(challenge.getImagePath())
                .exerciseType(challenge.getExerciseType())
                .exerciseCount(challenge.getExerciseCount())
                .exerciseDuration(challenge.getExerciseDuration())
                .exerciseDistance(challenge.getExerciseDistance())
                .build();
    }

}
