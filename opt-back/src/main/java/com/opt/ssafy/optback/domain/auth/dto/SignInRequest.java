package com.opt.ssafy.optback.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignInRequest {
    private String email;
}
