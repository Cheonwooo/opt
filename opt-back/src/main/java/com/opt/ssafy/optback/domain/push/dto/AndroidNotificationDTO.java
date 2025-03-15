package com.opt.ssafy.optback.domain.push.dto;

import lombok.Data;

@Data
public class AndroidNotificationDTO {
    private NotificationDto notification;
    private String priority = "high";
}