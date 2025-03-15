package com.opt.ssafy.optback.domain.session.dto;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSessionRecordRequest {
    private int id; // session_record의 ID
    @Nullable
    private Integer setCount;
    @Nullable
    private Integer repCount;
    @Nullable
    private Integer weight;
    @Nullable
    private Integer duration;
    @Nullable
    private Integer distance;
}
