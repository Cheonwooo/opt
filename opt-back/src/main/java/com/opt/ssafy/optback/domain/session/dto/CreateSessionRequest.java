package com.opt.ssafy.optback.domain.session.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateSessionRequest {
    private Integer ticketId;  // 옵션
    private int number;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
