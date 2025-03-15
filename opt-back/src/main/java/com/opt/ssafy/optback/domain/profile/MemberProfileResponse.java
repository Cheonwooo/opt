package com.opt.ssafy.optback.domain.profile;

import com.opt.ssafy.optback.domain.badge.dto.BadgeResponse;
import com.opt.ssafy.optback.domain.badge.entity.Badge;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.domain.member.entity.MemberInterest;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class MemberProfileResponse extends ProfileResponse {

    public static MemberProfileResponse from(Member member, Badge mainBadge) {
        return MemberProfileResponse.builder()
                .id(member.getId())
                .role(member.getRole().name())
                .imagePath(member.getImagePath())
                .mainBadge(new BadgeResponse(mainBadge))
                .nickname(member.getNickname())
                .interests(member.getMemberInterests().stream().map(MemberInterest::getInterest)
                        .collect(Collectors.toList()))
                .build();
    }

    public static TrainerProfileResponse from(Member member) {
        return TrainerProfileResponse.builder()
                .id(member.getId())
                .role(member.getRole().name())
                .name(member.getName())
                .nickname(member.getNickname())
                .imagePath(member.getImagePath())
                .interests(member.getMemberInterests().stream().map(MemberInterest::getInterest)
                        .collect(Collectors.toList()))
                .build();
    }
}
