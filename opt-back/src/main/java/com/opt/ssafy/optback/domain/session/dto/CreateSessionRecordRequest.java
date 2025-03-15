package com.opt.ssafy.optback.domain.session.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSessionRecordRequest {
    private int sessionId;
    private int exerciseId;
    private Integer setCount;
    private Integer repCount;
    private Integer weight;
    private Integer duration;
    private Integer distance;
}
