package com.opt.ssafy.optback.domain.challenge.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChallengeRecordRequest {
    private int memberId;
    private int challengeId;
    private Integer count;
    private Integer duration;
    private Integer distance;
}
