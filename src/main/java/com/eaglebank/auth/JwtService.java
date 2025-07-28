package com.eaglebank.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecretBase64;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretBase64);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .expiration(expiry)
                .issuedAt(now)
                .signWith(secretKey)
                .compact();
    }

    public Jws<Claims> readToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }

    public String extractUserId(String token) {
        return readToken(token).getPayload().getSubject();
    }

    void setJwtSecretBase64(String secret) {
        this.jwtSecretBase64 = secret;
    }

    void setJwtExpirationMs(long expirationMs) {
        this.jwtExpirationMs = expirationMs;
    }
}
