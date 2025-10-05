package com.tikkeul.mote.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FullParkingLotException.class)
    public ResponseEntity<Map<String, String>> handleFullParkingLotException(FullParkingLotException e) {
        Map<String, String> response = new HashMap<>();
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BlacklistConflictException.class)
    public ResponseEntity<Map<String, String>> handleBlacklistConflictException(BlacklistConflictException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        response.put("plate", ex.getPlate()); // 인식된 차량 번호 추가
        return new ResponseEntity<>(response, HttpStatus.CONFLICT); // 409 Conflict
    }
}
