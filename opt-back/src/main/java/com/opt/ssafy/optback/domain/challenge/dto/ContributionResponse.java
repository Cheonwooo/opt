package com.opt.ssafy.optback.domain.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContributionResponse {
    private int memberId;
    private String nickname;
    private double measurement;
    private double contributionPercentage;
    private boolean isMyRecord;

    public void setContributionPercentage(double contributionPercentage) {
        this.contributionPercentage = contributionPercentage;
    }

    public void setMeasurement(double v) {
        this.measurement = v;
    }
}
