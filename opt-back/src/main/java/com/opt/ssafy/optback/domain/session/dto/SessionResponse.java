package com.opt.ssafy.optback.domain.session.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SessionResponse {
    private int id;
    private Integer ticketId;
    private int number;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean isMemberSigned;
    private boolean isTrainerSigned;
}
