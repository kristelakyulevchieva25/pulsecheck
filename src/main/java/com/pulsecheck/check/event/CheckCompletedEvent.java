
package com.pulsecheck.check.event;

import java.time.Instant;

public record CheckCompletedEvent(
        Long monitorId,
        boolean success,
        Integer statusCode,
        long responseTimeMs,
        Instant checkedAt
) {
}
