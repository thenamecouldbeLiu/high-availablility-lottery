package com.interview.lottory.infra.exception;

import com.interview.lottory.infra.Constants;
import lombok.Getter;

@Getter
public final class ErrorCode {
    public static final ErrorCode SUCCESS =
            new ErrorCode("SUCCESS", ErrorType.SUCCESS, 200, Constants.MessageKey.SUCCESS);
    public static final ErrorCode INVALID_REQUEST =
            new ErrorCode("INVALID_REQUEST", ErrorType.VALIDATION, 400, Constants.MessageKey.INVALID_REQUEST);
    public static final ErrorCode CAMPAIGN_NOT_FOUND =
            new ErrorCode("CAMPAIGN_NOT_FOUND", ErrorType.NOT_FOUND, 404, Constants.MessageKey.CAMPAIGN_NOT_FOUND);
    public static final ErrorCode CAMPAIGN_NOT_ACTIVE =
            new ErrorCode("CAMPAIGN_NOT_ACTIVE", ErrorType.BUSINESS, 422, Constants.MessageKey.CAMPAIGN_NOT_ACTIVE);
    public static final ErrorCode PRIZE_NOT_FOUND =
            new ErrorCode("PRIZE_NOT_FOUND", ErrorType.NOT_FOUND, 404, Constants.MessageKey.PRIZE_NOT_FOUND);
    public static final ErrorCode DRAW_LIMIT_EXCEEDED =
            new ErrorCode("DRAW_LIMIT_EXCEEDED", ErrorType.BUSINESS, 422, Constants.MessageKey.DRAW_LIMIT_EXCEEDED);
    public static final ErrorCode OUT_OF_STOCK =
            new ErrorCode("OUT_OF_STOCK", ErrorType.BUSINESS, 422, Constants.MessageKey.OUT_OF_STOCK);
    public static final ErrorCode INVALID_PRIZE_CONFIGURATION =
            new ErrorCode("INVALID_PRIZE_CONFIGURATION", ErrorType.VALIDATION, 400,
                    Constants.MessageKey.INVALID_PRIZE_CONFIGURATION);
    public static final ErrorCode DUPLICATE_REQUEST =
            new ErrorCode("DUPLICATE_REQUEST", ErrorType.CONFLICT, 409, Constants.MessageKey.DUPLICATE_REQUEST);
    public static final ErrorCode MQ_OPERATION_FAILED =
            new ErrorCode("MQ_OPERATION_FAILED", ErrorType.INFRASTRUCTURE, 503, Constants.MessageKey.MQ_OPERATION_FAILED);
    public static final ErrorCode REDIS_OPERATION_FAILED =
            new ErrorCode("REDIS_OPERATION_FAILED", ErrorType.INFRASTRUCTURE, 503, Constants.MessageKey.REDIS_OPERATION_FAILED);
    public static final ErrorCode DATABASE_OPERATION_FAILED =
            new ErrorCode("DATABASE_OPERATION_FAILED", ErrorType.INFRASTRUCTURE, 503, Constants.MessageKey.DATABASE_OPERATION_FAILED);
    public static final ErrorCode INTERNAL_ERROR =
            new ErrorCode("INTERNAL_ERROR", ErrorType.INTERNAL, 500, Constants.MessageKey.INTERNAL_ERROR);

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
}
