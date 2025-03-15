package com.opt.ssafy.optback.domain.trainer_review.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrainerReviewRequest {
    private Integer trainerId;
    private String comment;
    private Integer rate;
}
