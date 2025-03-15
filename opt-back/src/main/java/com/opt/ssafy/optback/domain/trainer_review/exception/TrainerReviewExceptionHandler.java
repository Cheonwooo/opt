package com.opt.ssafy.optback.domain.trainer_review.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice(basePackages = "com.opt.ssafy.optback.domain.trainer_review")
public class TrainerReviewExceptionHandler {

    //리뷰 저장 실패
    @ExceptionHandler(TrainerReviewNotSaveException.class)
    ResponseEntity<Map<String, String>> handleSaveException(TrainerReviewNotSaveException e) {
        Map<String, String> map = new HashMap<>();
        map.put("error", e.getMessage());
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }

    // 데이터 조회 실패
    @ExceptionHandler(TrainerReviewNotFoundException.class)
    ResponseEntity<Map<String, String>> handleNotFoundException(TrainerReviewNotFoundException e) {
        Map<String, String> map = new HashMap<>();
        map.put("error", e.getMessage());
        return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
    }

    //기타 예외
    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> handleException(Exception e) {
        Map<String, String> map = new HashMap<>();
        map.put("error", e.getMessage());
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }
}
