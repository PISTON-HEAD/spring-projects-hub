package com.ragapp.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for JwtService.
 * Validates token creation, claim extraction, and validation using a test secret key.
 */
class JwtServiceTest {

    private JwtService jwtService;

    // 256-bit Base64-encoded HMAC key (test key — never use in production)
    private static final String TEST_SECRET =
            "dGVzdFNlY3JldEtleUZvckp3dEF1dGhUZXN0aW5nMTIzNDU2";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3_600_000L); // 1 hour
    }

    @Test
    @DisplayName("generateToken: produces a non-null JWT with three segments")
    void generateToken_returnsNonEmptyString() {
        String token = jwtService.generateToken("acme", "org:acme", "ORG", "Acme Corporation");

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("extract*: returns subject, scopeKey, role and display name from a valid token")
    void extract_returnsClaims() {
        String token = jwtService.generateToken("acme", "org:acme", "ORG", "Acme Corporation");

        assertThat(jwtService.extractUsername(token)).isEqualTo("acme");
        assertThat(jwtService.extractScopeKey(token)).isEqualTo("org:acme");
        assertThat(jwtService.extractRole(token)).isEqualTo("ORG");
        assertThat(jwtService.extractDisplayName(token)).isEqualTo("Acme Corporation");
    }

    @Test
    @DisplayName("isValid: returns true for a fresh token")
    void isValid_returnsTrueForFreshToken() {
        String token = jwtService.generateToken("guest-123", "ind:123", "INDIVIDUAL", "Individual workspace");

        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    @DisplayName("isValid: returns false for an expired token")
    void isValid_returnsFalseForExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", 1L);
        String token = jwtService.generateToken("guest-123", "ind:123", "INDIVIDUAL", "x");

        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        assertThat(jwtService.isValid(token)).isFalse();
    }

    @Test
    @DisplayName("isValid: returns false for a tampered/garbage token")
    void isValid_returnsFalseForTamperedToken() {
        assertThat(jwtService.isValid("not.a.valid.jwt")).isFalse();
    }

    @Test
    @DisplayName("parse: throws for a tampered/invalid token")
    void parse_throwsForInvalidToken() {
        assertThatThrownBy(() -> jwtService.parse("not.a.valid.jwt"))
                .isInstanceOf(Exception.class);
    }
}
