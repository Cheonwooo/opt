package com.opt.ssafy.optback.global.api;

import com.opt.ssafy.optback.global.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException() {
        ErrorResponse errorResponse = ErrorResponse.builder().message("권한이 없습니다").build();
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

}
