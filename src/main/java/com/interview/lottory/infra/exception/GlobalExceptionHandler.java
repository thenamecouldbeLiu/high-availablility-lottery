package com.interview.lottory.infra.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse(ErrorCode.INVALID_REQUEST.getDefaultMessage());
        return build(ErrorCode.INVALID_REQUEST, message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        return build(ErrorCode.INVALID_REQUEST, exception.getMessage(), request);
    }

    private ResponseEntity<ApiErrorResponse> build(
            ErrorCode errorCode, String message, HttpServletRequest request) {
        ApiErrorResponse response = new ApiErrorResponse(
                errorCode.getCode(),
                errorCode.getErrorType().name(),
                message,
                request.getRequestURI(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatusCode.valueOf(errorCode.getHttpStatus())).body(response);
    }
}
