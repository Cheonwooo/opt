package com.opt.ssafy.optback.domain.badge.dto;

import com.opt.ssafy.optback.domain.badge.entity.Badge;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BadgeResponse {

    private int id;
    private String name;
    private String description;
    private String imagePath;

    public BadgeResponse(Badge badge) {
        this.id = badge.getId();
        this.name = badge.getName();
        this.description = badge.getDescription();
        this.imagePath = badge.getImagePath();
    }
}
