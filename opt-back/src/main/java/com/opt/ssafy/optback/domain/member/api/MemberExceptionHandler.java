package com.opt.ssafy.optback.domain.member.api;

import com.opt.ssafy.optback.domain.member.exception.DuplicatedNicknameException;
import com.opt.ssafy.optback.global.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = MemberController.class)
public class MemberExceptionHandler {

    @ExceptionHandler(DuplicatedNicknameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatedNicknameException(DuplicatedNicknameException e) {
        return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.CONFLICT);
    }

}
