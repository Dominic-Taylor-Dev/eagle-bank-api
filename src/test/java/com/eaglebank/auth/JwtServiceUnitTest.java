package com.eaglebank.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JWT Service Unit Tests")
class JwtServiceUnitTest {

    private JwtService jwtService;

    // Example base64-encoded 256-bit secret key (must be 256-bit for HS256)
    private static final String TEST_SECRET_BASE64 = "uZQ20fzFjJmK6w62RklVqJtX2T+RL+Hzmvz2y9cHOQ4=";
    private static final long TEST_EXPIRATION_MS = 3600000; // 1 hour

    private String generatedToken;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        jwtService.setJwtSecretBase64(TEST_SECRET_BASE64);
        jwtService.setJwtExpirationMs(TEST_EXPIRATION_MS);

        jwtService.init();

        generatedToken = jwtService.generateToken("user-123", "user@example.com");
    }

    @Test
    @DisplayName("Generate token contains expected subject and email claims")
    void testGenerateToken() {
        assertThat(generatedToken).isNotNull();

        Jws<Claims> claimsJws = jwtService.readToken(generatedToken);

        assertThat(claimsJws.getPayload().getSubject()).isEqualTo("user-123");
        assertThat(claimsJws.getPayload().get("email", String.class)).isEqualTo("user@example.com");
        assertThat(claimsJws.getPayload().getExpiration()).isAfter(claimsJws.getPayload().getIssuedAt());
    }

    @Test
    @DisplayName("Extract userId from valid token returns correct subject")
    void testExtractUserId() {
        String userId = jwtService.extractUserId(generatedToken);
        assertThat(userId).isEqualTo("user-123");
    }

    @Test
    @DisplayName("Read token with invalid signature throws JwtException")
    void testReadTokenInvalidSignature() {
        // Tamper the token by changing a character (simple corruption)
        String tamperedToken = generatedToken.substring(0, generatedToken.length() - 1) + "a";

        assertThatThrownBy(() -> jwtService.readToken(tamperedToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Read token with malformed token throws JwtException")
    void testReadTokenMalformed() {
        String malformedToken = "not.a.valid.token";

        assertThatThrownBy(() -> jwtService.readToken(malformedToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Read token with expired token throws JwtException")
    void testReadTokenExpired() throws InterruptedException {
        // Set expiration to 1 ms
        jwtService.setJwtExpirationMs(1);

        String shortLivedToken = jwtService.generateToken("user-123", "user@example.com");

        // Wait 10 ms to ensure token expiration
        Thread.sleep(10);

        assertThatThrownBy(() -> jwtService.readToken(shortLivedToken))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("expired");
    }
}
