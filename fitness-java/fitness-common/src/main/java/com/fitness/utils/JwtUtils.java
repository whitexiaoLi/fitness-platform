package com.fitness.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expire}")
    private int accessTokenExpire;

    @Value("${jwt.refresh-token-expire}")
    private int refreshTokenExpire;

    public String generateAccessToken(Long userId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpire * 60 * 1000L))
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpire * 24 * 60 * 60 * 1000L))
                .signWith(getKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUserId(String token) {
        return parseToken(token).getSubject();
    }

    public String getJti(String token) {
        return parseToken(token).getId();
    }

    public long getTokenExpire(String token) {
        return parseToken(token).getExpiration().getTime() - System.currentTimeMillis();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
