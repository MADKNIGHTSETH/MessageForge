package com.messageforge.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    private static final long DEFAULT_ACCESS_TOKEN_EXPIRATION_MS = 900_000L;
    private static final long DEFAULT_REFRESH_TOKEN_EXPIRATION_MS = 604_800_000L;
    private static final int HS512_MINIMUM_SECRET_BYTES = 64;

    private final long jwtExpiration;
    private final long refreshTokenExpiration;
    private final SecretKey signingKey;

    public JwtTokenProvider() {
        String jwtSecret = AppProperties.getRequired("JWT signing secret", "JWT_SECRET", "app.jwt.secret");
        this.jwtExpiration = AppProperties.getLong(
                DEFAULT_ACCESS_TOKEN_EXPIRATION_MS,
                "JWT_EXPIRATION_MS",
                "app.jwt.expiration");
        this.refreshTokenExpiration = AppProperties.getLong(
                DEFAULT_REFRESH_TOKEN_EXPIRATION_MS,
                "JWT_REFRESH_EXPIRATION_MS",
                "app.jwt.refresh-expiration");
        this.signingKey = createSigningKey(jwtSecret);
    }

    public String generateAccessToken(String email) {
        return createToken(email, jwtExpiration);
    }

    public String generateRefreshToken(String email) {
        return createToken(email, refreshTokenExpiration);
    }

    public long getAccessTokenExpirationMillis() {
        return jwtExpiration;
    }

    private String createToken(String email, long expirationTime) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(signingKey, Jwts.SIG.HS512)
                .compact();
    }

    public String extractEmail(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getSubject();
        } catch (JwtException e) {
            log.warn("Error extracting email from token", e);
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed", e);
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }

    public long getExpirationTime(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().getTime() - System.currentTimeMillis();
        } catch (JwtException | IllegalArgumentException e) {
            return 0;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey createSigningKey(String jwtSecret) {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < HS512_MINIMUM_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET must be at least 64 bytes for HS512 signing");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
