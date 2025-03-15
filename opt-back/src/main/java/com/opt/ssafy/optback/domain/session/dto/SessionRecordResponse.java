package com.opt.ssafy.optback.domain.session.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SessionRecordResponse {
    private int id;
    private int sessionId;
    private int exerciseId;
    private Integer setCount;
    private Integer repCount;
    private Integer weight;
    private Integer duration;
    private Integer distance;
}
