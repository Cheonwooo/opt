package com.opt.ssafy.optback.domain.push.dto;

import lombok.Setter;

@Setter
public class NotificationDto {
    private Object title;
    private Object body;
    private String sound = "default";
    private String channelId = "500";
}
