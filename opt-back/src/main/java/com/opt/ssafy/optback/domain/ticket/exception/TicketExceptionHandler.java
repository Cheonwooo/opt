package com.opt.ssafy.optback.domain.ticket.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "com.opt.ssafy.optback.domain.ticket")
public class TicketExceptionHandler {

    // 티켓 저장 실패
    @ExceptionHandler(TicketNotSaveException.class)
    ResponseEntity<Map<String, String>> handleSaveException(TicketNotSaveException e) {
        Map<String, String> map = new HashMap<>();
        map.put("error", e.getMessage());
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }

    // 데이터 조회 실패
    @ExceptionHandler(TicketNotFoundException.class)
    ResponseEntity<Map<String, String>> handleNotFoundException(TicketNotFoundException e) {
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
