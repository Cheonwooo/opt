package com.opt.ssafy.optback.domain.badge.event;

import com.opt.ssafy.optback.domain.badge.dto.ActivityType;
import com.opt.ssafy.optback.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ActivityEvent {

    private final Member member;
    private final ActivityType activityType;

}
