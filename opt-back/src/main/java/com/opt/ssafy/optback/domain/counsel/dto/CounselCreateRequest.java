package com.opt.ssafy.optback.domain.counsel.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class CounselCreateRequest {
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    // 상담 등록 시, 상담 받을 트레이너의 ID를 클라이언트가 전달합니다.
    private int trainerId;
}
