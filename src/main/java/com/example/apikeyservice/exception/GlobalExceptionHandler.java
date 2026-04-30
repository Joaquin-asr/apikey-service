package com.example.apikeyservice.exception;

import com.example.apikeyservice.constants.ApiKeyConstants;
import com.example.apikeyservice.dto.ApiKeyResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiKeyValidationException.class)
    public ResponseEntity<ApiKeyResponseDto> handleValidationException(ApiKeyValidationException ex) {
        HttpStatus status = ApiKeyConstants.CODE_COOLDOWN_ACTIVE.equals(ex.getCode())
                ? HttpStatus.TOO_MANY_REQUESTS
                : HttpStatus.UNAUTHORIZED;
        log.warn("[ERROR] code={} message={}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(status)
                .body(ApiKeyResponseDto.failure(ex.getMessage(), ex.getCode()));
    }
}
