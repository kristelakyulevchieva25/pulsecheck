package com.pulsecheck.event;

import java.time.Instant;

public record IncidentOpenedEvent(
        Long incidentId,
        Long monitorId,
        String cause,
        Instant startedAt
) {}