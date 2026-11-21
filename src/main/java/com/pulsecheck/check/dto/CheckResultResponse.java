
package com.pulsecheck.check.dto;

import java.time.Instant;

public record CheckResultResponse(
        Long id,
        Long monitorId,
        boolean success,
        Integer statusCode,
        long responseTimeMs,
        Instant checkedAt
) {
}
