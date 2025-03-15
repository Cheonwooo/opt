package com.opt.ssafy.optback.domain.session.dto;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateSessionRequest {
    private int id;
    @Nullable
    private LocalDateTime startAt;
    @Nullable
    private LocalDateTime endAt;
}
