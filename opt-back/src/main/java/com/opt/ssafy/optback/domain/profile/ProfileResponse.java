package com.opt.ssafy.optback.domain.profile;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.opt.ssafy.optback.domain.badge.dto.BadgeResponse;
import com.opt.ssafy.optback.domain.member.entity.Interest;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class ProfileResponse {
    private String role;
    private Integer id;
    private String imagePath;
    private String nickname;
    private BadgeResponse mainBadge;
    private List<Interest> interests;

    @JsonIgnore // 필드 직렬화를 무시
    private boolean isFollow;

    @JsonGetter("isFollow") // getter에서 "isFollow"라는 이름으로 직렬화
    public boolean isFollow() {
        return isFollow;
    }

}
