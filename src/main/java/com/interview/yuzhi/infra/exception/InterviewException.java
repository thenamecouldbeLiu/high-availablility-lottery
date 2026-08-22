package com.interview.yuzhi.infra.exception;

import lombok.Getter;

@Getter
public class InterviewException extends RuntimeException {
    private final ErrorCode errorCode;
    private final ErrorType errorType;

    public InterviewException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), null);
    }

    public InterviewException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public InterviewException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = errorCode.getErrorType();
    }
}
