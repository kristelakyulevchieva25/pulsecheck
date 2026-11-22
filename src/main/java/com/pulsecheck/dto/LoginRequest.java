package com.pulsecheck.dto;

import com.pulsecheck.validation.MaxBytes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @MaxBytes(value = 72, message = "Password must not exceed 72 bytes")
        String password
) {
}