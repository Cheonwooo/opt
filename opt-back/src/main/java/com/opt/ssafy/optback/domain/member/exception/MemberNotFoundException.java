package com.opt.ssafy.optback.domain.member.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException() {
        super("찾을 수 없는 유저입니다");
    }
}
