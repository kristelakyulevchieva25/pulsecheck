package com.pulsecheck.dto;

import com.pulsecheck.entity.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        Role role,
        Instant createdAt
) {
}
