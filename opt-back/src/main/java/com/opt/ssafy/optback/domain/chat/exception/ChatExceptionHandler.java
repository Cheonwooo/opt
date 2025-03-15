package com.opt.ssafy.optback.domain.chat.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "com.opt.ssafy.optback.domain.chat")
public class ChatExceptionHandler {

    @ExceptionHandler(ChatRoomException.class)
    ResponseEntity<Map<String, String>> handleSaveException(ChatRoomException e) {
        Map<String, String> map = new HashMap<>();
        map.put("error", e.getMessage());
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ChatMessageException.class)
    ResponseEntity<Map<String, String>> handleNotFoundException(ChatMessageException e) {
        Map<String, String> map = new HashMap<>();
        map.put("error", e.getMessage());
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }

    //기타 예외
    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> handleException(Exception e) {
        Map<String, String> map = new HashMap<>();
        map.put("error", e.getMessage());
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }
}