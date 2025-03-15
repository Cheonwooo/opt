package com.opt.ssafy.optback.domain.challenge.exception;

public class ChallengeCreationException extends RuntimeException {
    public ChallengeCreationException(String message) {
        super(message);
    }
    public ChallengeCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
