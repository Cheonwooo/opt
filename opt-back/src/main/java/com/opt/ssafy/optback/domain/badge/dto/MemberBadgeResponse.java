package com.opt.ssafy.optback.domain.badge.dto;

import com.opt.ssafy.optback.domain.badge.entity.MemberBadge;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberBadgeResponse {

    private int id;
    private int badgeId;
    private LocalDate createDate;

    public MemberBadgeResponse(MemberBadge memberBadge) {
        this.id = memberBadge.getId();
        this.badgeId = memberBadge.getBadge().getId();
        this.createDate = memberBadge.getCreateDate();
    }

}
