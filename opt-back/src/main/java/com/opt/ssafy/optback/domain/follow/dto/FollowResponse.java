package com.opt.ssafy.optback.domain.follow.dto;

import com.opt.ssafy.optback.domain.follow.entity.Follow;
import com.opt.ssafy.optback.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FollowResponse {
    private int id;
    private int memberId;
    private String name;
    private String nickname;
    private String imagePath;
    private String role;

    public static FollowResponse fromFollowingEntity(Follow follow) {
        Member target = follow.getTarget();
        return FollowResponse.builder()
                .id(follow.getId())
                .memberId(target.getId())
                .name(target.getName())
                .nickname(target.getNickname())
                .imagePath(target.getImagePath())
                .role(target.getRole().name())
                .build();
    }

    public static FollowResponse fromFollowerEntity(Follow follow) {
        Member follower = follow.getMember();
        return FollowResponse.builder()
                .id(follow.getId())
                .memberId(follower.getId())
                .name(follower.getName())
                .nickname(follower.getNickname())
                .imagePath(follower.getImagePath())
                .role(follower.getRole().name())
                .build();
    }
}
