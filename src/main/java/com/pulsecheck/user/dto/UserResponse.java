package com.pulsecheck.user.dto;

import com.pulsecheck.user.entity.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        Role role,
        Instant createdAt
) {
}
