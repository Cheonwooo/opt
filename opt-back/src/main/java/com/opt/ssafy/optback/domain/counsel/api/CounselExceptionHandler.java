package com.opt.ssafy.optback.domain.counsel.api;

import com.opt.ssafy.optback.domain.counsel.exception.CounselNotFoundException;
import com.opt.ssafy.optback.domain.counsel.exception.TrainerNotFoundException;
import com.opt.ssafy.optback.global.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CounselExceptionHandler {

    @ExceptionHandler(CounselNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCounselNotFoundException(CounselNotFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(TrainerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTrainerNotFoundException(TrainerNotFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .build();
        // 트레이너가 존재하지 않으면 404 (NOT_FOUND)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

}
