package com.interview.yuzhi.infra.exception;

import java.time.Instant;

public record ApiErrorResponse(
        String errorCode,
        String errorType,
        String message,
        String path,
        Instant timestamp
) {
}
