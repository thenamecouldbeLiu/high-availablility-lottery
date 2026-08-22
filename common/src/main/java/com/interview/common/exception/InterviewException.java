package com.interview.common.exception;

public class InterviewException extends RuntimeException {
    private final ErrorCode errorCode;
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
        this.messageKey = messageKey;
        this.messageArguments = messageArguments == null ? new Object[0] : messageArguments.clone();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public ErrorType getErrorType() {
        return errorCode.getErrorType();
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getMessageArguments() {
        return messageArguments.clone();
    }
}
