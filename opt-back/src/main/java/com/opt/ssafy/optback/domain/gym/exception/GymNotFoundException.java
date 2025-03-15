package com.opt.ssafy.optback.domain.gym.exception;

public class GymNotFoundException extends RuntimeException {
    public GymNotFoundException(String message) {
        super(message);
    }
}
