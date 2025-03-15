package com.opt.ssafy.optback.domain.session.exception;

public class SessionRecordNotFoundException extends RuntimeException {
    public SessionRecordNotFoundException(String message) {
        super(message);
    }
}
