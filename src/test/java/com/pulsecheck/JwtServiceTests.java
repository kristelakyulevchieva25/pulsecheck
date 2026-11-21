package com.pulsecheck;

import com.pulsecheck.service.JwtService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTests {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-characters-long";

    private final JwtService jwtService = new JwtService(SECRET, 60_000);

    @Test
    void generatedTokenIsValidAndContainsEmail() {
        String token = jwtService.generateToken("user@example.com");

        assertTrue(jwtService.isValid(token));
        assertEquals("user@example.com", jwtService.extractEmail(token));
    }

    @Test
    void tokenSignedWithAnotherSecretIsInvalid() {
        JwtService other = new JwtService("another-secret-key-that-is-at-least-32-characters", 60_000);
        String foreignToken = other.generateToken("user@example.com");

        assertFalse(jwtService.isValid(foreignToken));
    }

    @Test
    void expiredTokenIsInvalid() {
        JwtService expiring = new JwtService(SECRET, -1000);
        String expiredToken = expiring.generateToken("user@example.com");

        assertFalse(jwtService.isValid(expiredToken));
    }

    @Test
    void garbageTokenIsInvalid() {
        assertFalse(jwtService.isValid("not-a-token"));
    }
}