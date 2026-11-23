package com.pulsecheck.dto;

import com.pulsecheck.entity.TeamPlan;

import java.time.Instant;

public record TeamResponse(
        Long id,
        String name,
        TeamPlan plan,
        Instant createdAt
) {
}