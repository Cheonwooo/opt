package com.opt.ssafy.optback.domain.onboarding.exception;

public class AlreadyOnboardedException extends RuntimeException {
    public AlreadyOnboardedException(String message) {
        super(message);
    }
}
