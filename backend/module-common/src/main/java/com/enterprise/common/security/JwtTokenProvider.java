package com.enterprise.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * @file JwtTokenProvider.java
 * @description JWT Token 產生與解析工具 / JWT Provider
 * @description_en Generate and validate JWT tokens
 * @description_zh 提供生成 JWT、解析 UUID 以及驗證 Token 是否有效的功能
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567843b12f12}")
    private String jwtSecret;

    @Value("${jwt.expiration:900000}")
    private long jwtExpirationInMs;

    public String generateToken(UUID userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(key())
                .compact();
    }

    public UUID getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return UUID.fromString(claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key()).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // log the error
        }
        return false;
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
