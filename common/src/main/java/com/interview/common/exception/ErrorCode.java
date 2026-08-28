package com.interview.common.exception;

public final class ErrorCode {
    public static final ErrorCode SUCCESS =
            new ErrorCode("SUCCESS", ErrorType.SUCCESS, 200, "error.success");
    public static final ErrorCode INVALID_REQUEST =
            new ErrorCode("INVALID_REQUEST", ErrorType.VALIDATION, 400, "error.invalid-request");
    public static final ErrorCode AUTHENTICATION_REQUIRED =
            new ErrorCode("AUTHENTICATION_REQUIRED", ErrorType.SECURITY, 401, "error.authentication-required");
    public static final ErrorCode ACCESS_DENIED =
            new ErrorCode("ACCESS_DENIED", ErrorType.SECURITY, 403, "error.access-denied");
    public static final ErrorCode USER_NOT_FOUND =
            new ErrorCode("USER_NOT_FOUND", ErrorType.NOT_FOUND, 404, "error.user-not-found");
    public static final ErrorCode CAMPAIGN_NOT_FOUND =
            new ErrorCode("CAMPAIGN_NOT_FOUND", ErrorType.NOT_FOUND, 404, "error.campaign-not-found");
    public static final ErrorCode CAMPAIGN_NOT_ACTIVE =
            new ErrorCode("CAMPAIGN_NOT_ACTIVE", ErrorType.BUSINESS, 422, "error.campaign-not-active");
    public static final ErrorCode PRIZE_NOT_FOUND =
            new ErrorCode("PRIZE_NOT_FOUND", ErrorType.NOT_FOUND, 404, "error.prize-not-found");
    public static final ErrorCode DRAW_LIMIT_EXCEEDED =
            new ErrorCode("DRAW_LIMIT_EXCEEDED", ErrorType.BUSINESS, 422, "error.draw-limit-exceeded");
    public static final ErrorCode OUT_OF_STOCK =
            new ErrorCode("OUT_OF_STOCK", ErrorType.BUSINESS, 422, "error.out-of-stock");
    public static final ErrorCode INVALID_PRIZE_CONFIGURATION =
            new ErrorCode("INVALID_PRIZE_CONFIGURATION", ErrorType.VALIDATION, 400,
                    "error.invalid-prize-configuration");
    public static final ErrorCode DUPLICATE_REQUEST =
            new ErrorCode("DUPLICATE_REQUEST", ErrorType.CONFLICT, 409, "error.duplicate-request");
    public static final ErrorCode MQ_OPERATION_FAILED =
            new ErrorCode("MQ_OPERATION_FAILED", ErrorType.INFRASTRUCTURE, 503, "error.mq-operation-failed");
    public static final ErrorCode REDIS_OPERATION_FAILED =
            new ErrorCode("REDIS_OPERATION_FAILED", ErrorType.INFRASTRUCTURE, 503, "error.redis-operation-failed");
    public static final ErrorCode DATABASE_OPERATION_FAILED =
            new ErrorCode("DATABASE_OPERATION_FAILED", ErrorType.INFRASTRUCTURE, 503,
                    "error.database-operation-failed");
    public static final ErrorCode KEYCLOAK_OPERATION_FAILED =
            new ErrorCode("KEYCLOAK_OPERATION_FAILED", ErrorType.INFRASTRUCTURE, 502,
                    "error.keycloak-operation-failed");
    public static final ErrorCode INTERNAL_ERROR =
            new ErrorCode("INTERNAL_ERROR", ErrorType.INTERNAL, 500, "error.internal");

    private final String code;
    private final ErrorType errorType;
    private final int httpStatus;
    private final String messageKey;

    private ErrorCode(String code, ErrorType errorType, int httpStatus, String messageKey) {
        this.code = code;
        this.errorType = errorType;
        this.httpStatus = httpStatus;
        this.messageKey = messageKey;
    }

    public String getCode() {
        return code;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
