package com.myblog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${app.jwt-secret}") String secret,
            @Value("${app.jwt-expiration-hours}") long expirationHours
    ) {
        byte[] secretBytes;
        try {
            secretBytes = Decoders.BASE64.decode(secret);
        } catch (Exception ignored) {
            secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET 至少需要 32 个字符");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMillis = expirationHours * 60 * 60 * 1000;
    }

    public String createToken(UserPrincipal principal) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(principal.userId()))
                .claim("username", principal.username())
                .claim("role", principal.role())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMillis))
                .signWith(signingKey)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(signingKey).build()
                .parseSignedClaims(token).getPayload();
    }
}
