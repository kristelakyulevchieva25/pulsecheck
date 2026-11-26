package com.pulsecheck.dto;

import com.pulsecheck.entity.Incident;
import com.pulsecheck.entity.IncidentStatus;

import java.time.Instant;

public record IncidentResponse(
        Long id,
        Long monitorId,
        IncidentStatus status,
        String cause,
        Instant startedAt,
        Instant acknowledgedAt,
        Instant resolvedAt,
        Boolean escalated
) {
    public static IncidentResponse from(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getMonitor().getId(),
                incident.getStatus(),
                incident.getCause(),
                incident.getStartedAt(),
                incident.getAcknowledgedAt(),
                incident.getResolvedAt(),
                incident.getEscalated()
        );
    }
}