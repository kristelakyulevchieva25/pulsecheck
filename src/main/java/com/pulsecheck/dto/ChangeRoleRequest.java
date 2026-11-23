package com.pulsecheck.dto;

import com.pulsecheck.entity.TeamRole;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(
        @NotNull(message = "Role is required")
        TeamRole role
) {
}