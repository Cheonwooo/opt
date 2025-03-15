package com.opt.ssafy.optback.domain.counsel.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class CounselResponse {
    private int id;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private int memberId;
    private int trainerId;
    private String status;
}
