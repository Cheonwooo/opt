package com.opt.ssafy.optback.domain.badge.entity;

import com.opt.ssafy.optback.domain.badge.dto.ActivityType;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BadgeDefinition {

    private final int id;

    private final int badgeId;

    private final ActivityType activityType;

    private final Map<String, Object> condition;
}
