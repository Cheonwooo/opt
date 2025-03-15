package com.opt.ssafy.optback.domain.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@AllArgsConstructor
public class OnboardingRequest {
    private final String nickname;
    private final List<Integer> interestIds;
}
