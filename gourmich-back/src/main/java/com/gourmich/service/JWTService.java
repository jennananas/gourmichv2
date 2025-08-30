package com.gourmich.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {
    @Value("${JWT_SECRET:}")
    String secretKey;

    private SecretKey key;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isBlank()) {
            key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        } else {
            key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        }
    }

    public String generateToken(String username) {
        return Jwts.builder().subject(username).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getKey())
                .compact();
    }

    SecretKey getKey() {
        if (secretKey == null || secretKey.isBlank()) {
            return key;
        }
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUserName(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            return true;
        }
    }

    Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return e.getClaims().getExpiration();

        }
    }

    public String generateTokenWithCustomExpiration(String username, long durationMillis) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + durationMillis))
                .signWith(key)
                .compact();
    }
}
