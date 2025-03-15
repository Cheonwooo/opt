package com.opt.ssafy.optback.domain.schedule.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ScheduleResponse {
    private int id;
    private String type; // "COUNSEL" 또는 "SESSION"
    private int memberId;
    private int trainerId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String status; // 상담의 경우 상태값 (APPROVED, PENDING, CANCELLED)
    private int ticketId; // PT 세션의 경우 티켓 ID
    private int number; // PT 세션 회차
    private boolean isMemberSigned; // PT 세션의 경우
    private boolean isTrainerSigned;
}
