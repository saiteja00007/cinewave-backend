package com.cinewave.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT token utility: generation and validation.
 * Uses HMAC-SHA256 signing with a configurable secret.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${cinewave.jwt.secret}") String secret,
            @Value("${cinewave.jwt.expiration-ms}") long expirationMs) {
        // Ensure the secret is long enough for HS256
        byte[] keyBytes;
        if (secret.length() < 43) { // Less than 256 bits in base64
            // Pad to ensure minimum length
            secret = secret + "0".repeat(43 - secret.length());
        }
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception e) {
            // If not valid base64, use raw bytes
            keyBytes = secret.getBytes();
        }
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT expired");
        } catch (JwtException ex) {
            log.warn("Invalid JWT: {}", ex.getMessage());
        }
        return false;
    }
}
