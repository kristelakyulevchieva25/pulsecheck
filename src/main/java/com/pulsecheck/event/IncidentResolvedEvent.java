package com.pulsecheck.event;

import java.time.Instant;

public record IncidentResolvedEvent(
        Long incidentId,
        Long monitorId,
        Instant resolvedAt
) {}