package com.opt.ssafy.optback.domain.counsel.dto;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CounselUpdateRequest {
    // 수정할 상담 일정의 ID
    private int id;
    @Nullable
    private LocalDateTime startAt;
    @Nullable
    private LocalDateTime endAt;
    // 상담 상태를 수정할 수 있도록 status 필드 추가 (예: "APPROVED", "CANCELLED" 등)
    private String status;
}
