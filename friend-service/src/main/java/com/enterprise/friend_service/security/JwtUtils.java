package com.enterprise.friend_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

// ? @Component makes this injectable across the security layer.
// ? The secret is injected from application.properties so it stays in one place.
@Component
public class JwtUtils {

    private final SecretKey key;

    public JwtUtils(@Value("${jwt.secret}") String secret) {
        // * Derive the same HMAC-SHA key the IAM service used to sign the token.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // * Validates the token signature and expiry, then returns the subject (email).
    // ! Throws a JwtException (unchecked) if the token is invalid or expired —
    // ! the filter catches this and returns 401.
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}