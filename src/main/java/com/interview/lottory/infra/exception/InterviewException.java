package com.interview.lottory.infra.exception;

import lombok.Getter;

@Getter
public class InterviewException extends RuntimeException {
    private final ErrorCode errorCode;
    private final ErrorType errorType;
    private final String messageKey;
    private final Object[] messageArguments;

    public InterviewException(ErrorCode errorCode) {
        this(errorCode, (Throwable) null, errorCode.getMessageKey());
    }

    public InterviewException(ErrorCode errorCode, String messageKey, Object... messageArguments) {
        this(errorCode, null, messageKey, messageArguments);
    }

    public InterviewException(ErrorCode errorCode, Throwable cause, String messageKey, Object... messageArguments) {
        super(messageKey, cause);
        this.errorCode = errorCode;
        this.errorType = errorCode.getErrorType();
        this.messageKey = messageKey;
        this.messageArguments = messageArguments == null ? new Object[0] : messageArguments.clone();
    }

    public Object[] getMessageArguments() {
        return messageArguments.clone();
    }
}
