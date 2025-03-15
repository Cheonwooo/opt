package com.opt.ssafy.optback.domain.badge.evaluator;

import com.opt.ssafy.optback.domain.badge.dto.ActivityType;
import com.opt.ssafy.optback.domain.member.entity.Member;
import java.util.Map;

public interface BadgeEvaluator {
    
    boolean evaluate(Member member, Map<String, Object> condition);

    // 업적 유형 매핑용
    ActivityType getType();

}
