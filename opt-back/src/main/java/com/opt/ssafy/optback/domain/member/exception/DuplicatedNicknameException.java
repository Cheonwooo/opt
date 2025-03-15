package com.opt.ssafy.optback.domain.member.exception;

public class DuplicatedNicknameException extends RuntimeException {
    public DuplicatedNicknameException() {
        super("중복된 아이디입니다");
    }
}
