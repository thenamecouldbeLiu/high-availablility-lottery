package com.interview.yuzhi.infra.exception;

import lombok.Getter;

@Getter
public final class ErrorCode {
    public static final ErrorCode SUCCESS =
            new ErrorCode("SUCCESS", ErrorType.SUCCESS, 200, "操作成功");
    public static final ErrorCode INVALID_REQUEST =
            new ErrorCode("INVALID_REQUEST", ErrorType.VALIDATION, 400, "請求資料不正確");
    public static final ErrorCode CAMPAIGN_NOT_FOUND =
            new ErrorCode("CAMPAIGN_NOT_FOUND", ErrorType.NOT_FOUND, 404, "找不到抽獎活動");
    public static final ErrorCode CAMPAIGN_NOT_ACTIVE =
            new ErrorCode("CAMPAIGN_NOT_ACTIVE", ErrorType.BUSINESS, 422, "抽獎活動目前不可用");
    public static final ErrorCode PRIZE_NOT_FOUND =
            new ErrorCode("PRIZE_NOT_FOUND", ErrorType.NOT_FOUND, 404, "找不到獎品");
    public static final ErrorCode DRAW_LIMIT_EXCEEDED =
            new ErrorCode("DRAW_LIMIT_EXCEEDED", ErrorType.BUSINESS, 422, "已超過允許的抽獎次數");
    public static final ErrorCode OUT_OF_STOCK =
            new ErrorCode("OUT_OF_STOCK", ErrorType.BUSINESS, 422, "獎品庫存不足");
    public static final ErrorCode INVALID_PRIZE_CONFIGURATION =
            new ErrorCode("INVALID_PRIZE_CONFIGURATION", ErrorType.VALIDATION, 400,
                    "活動必須有 3 種獎品與 1 個銘謝惠顧，且機率總和必須為 100%");
    public static final ErrorCode DUPLICATE_REQUEST =
            new ErrorCode("DUPLICATE_REQUEST", ErrorType.CONFLICT, 409, "此抽獎請求已處理");
    public static final ErrorCode MQ_OPERATION_FAILED =
            new ErrorCode("MQ_OPERATION_FAILED", ErrorType.INFRASTRUCTURE, 503, "訊息佇列操作失敗");
    public static final ErrorCode REDIS_OPERATION_FAILED =
            new ErrorCode("REDIS_OPERATION_FAILED", ErrorType.INFRASTRUCTURE, 503, "Redis 操作失敗");
    public static final ErrorCode DATABASE_OPERATION_FAILED =
            new ErrorCode("DATABASE_OPERATION_FAILED", ErrorType.INFRASTRUCTURE, 503, "資料庫操作失敗");
    public static final ErrorCode INTERNAL_ERROR =
            new ErrorCode("INTERNAL_ERROR", ErrorType.INTERNAL, 500, "系統發生未預期錯誤");

    private final String code;
    private final ErrorType errorType;
    private final int httpStatus;
    private final String defaultMessage;

    private ErrorCode(String code, ErrorType errorType, int httpStatus, String defaultMessage) {
        this.code = code;
        this.errorType = errorType;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
