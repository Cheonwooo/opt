package com.opt.ssafy.optback.domain.member.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class UpdateInterestRequest {
    private List<Integer> interestIds;
}
