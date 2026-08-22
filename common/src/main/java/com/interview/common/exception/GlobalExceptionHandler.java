package com.interview.common.exception;

import com.interview.common.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(InterviewException.class)
    public ResponseEntity<Response<Void>> handleInterviewException(
            InterviewException exception, HttpServletRequest request) {
        String message = getMessage(exception.getMessageKey(), exception.getMessageArguments());
        return build(exception.getErrorCode(), message, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleValidationException(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElseGet(() -> getMessage(ErrorCode.INVALID_REQUEST.getMessageKey()));
        return build(ErrorCode.INVALID_REQUEST, message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response<Void>> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        return build(ErrorCode.INVALID_REQUEST, exception.getMessage(), request);
    }

    private ResponseEntity<Response<Void>> build(
            ErrorCode errorCode, String message, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatusCode.valueOf(errorCode.getHttpStatus()))
                .body(Response.error(errorCode, message, request.getRequestURI()));
    }

    private String getMessage(String messageKey, Object... arguments) {
        return messageSource.getMessage(messageKey, arguments, messageKey, LocaleContextHolder.getLocale());
    }
}
