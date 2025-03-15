package com.opt.ssafy.optback.domain.trainer_review.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrainerReviewSummaryResponse {
    private Integer count;
    private Double averageRate;
}
