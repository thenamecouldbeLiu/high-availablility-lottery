package com.interview.yuzhi.infra.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InterviewException.class)
    public ResponseEntity<ApiErrorResponse> handleInterviewException(
            InterviewException exception,
            HttpServletRequest request) {
        ApiErrorResponse response = new ApiErrorResponse(
                exception.getErrorCode().getCode(),
                exception.getErrorType().name(),
                exception.getMessage(),
                request.getRequestURI(),
                Instant.now()
        );
        HttpStatusCode status = HttpStatusCode.valueOf(exception.getErrorCode().getHttpStatus());
        return ResponseEntity.status(status).body(response);
    }
}
