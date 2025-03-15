package com.opt.ssafy.optback.domain.auth.exception;

public class DuplicatedSignUpException extends RuntimeException {
    public DuplicatedSignUpException() {
        super("중복된 회원가입");
    }
}
