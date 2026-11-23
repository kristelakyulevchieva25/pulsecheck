package com.pulsecheck.dto;

import com.pulsecheck.entity.TeamRole;

import java.time.Instant;

public record MemberResponse(
        Long userId,
        String email,
        TeamRole teamRole,
        Instant joinedAt
) {
}