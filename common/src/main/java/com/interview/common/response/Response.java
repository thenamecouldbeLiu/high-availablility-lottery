package com.interview.common.response;

import com.interview.common.exception.ErrorCode;
import com.interview.common.exception.ErrorType;

import java.time.Instant;

public final class Response<T> {
    private final boolean success;
    private final String code;
    private final ErrorType errorType;
    private final String message;
    private final T data;
    private final String path;
    private final Instant timestamp;

    private Response(boolean success, String code, ErrorType errorType, String message,
                     T data, String path, Instant timestamp) {
        this.success = success;
        this.code = code;
        this.errorType = errorType;
        this.message = message;
        this.data = data;
        this.path = path;
        this.timestamp = timestamp;
    }

    public static <T> Response<T> success(T data) {
        return new Response<>(true, ErrorCode.SUCCESS.getCode(), ErrorType.SUCCESS,
                null, data, null, Instant.now());
    }

    public static Response<Void> success() {
        return success(null);
    }

    public static <T> Response<T> error(ErrorCode errorCode, String message, String path) {
        return new Response<>(false, errorCode.getCode(), errorCode.getErrorType(),
                message, null, path, Instant.now());
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getPath() {
        return path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
