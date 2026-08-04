package io.datasheild.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${datasheild.auth.jwt.secret:dev-secret-key-change-in-production-32-chars-min}")
    private String jwtSecret;

    @Value("${datasheild.auth.jwt.expiration:3600000}")  // 1 hour default
    private long jwtExpiration;

    @Value("${datasheild.auth.jwt.refresh-expiration:604800000}")  // 7 days default
    private long refreshTokenExpiration;

    private static final String CLAIM_TENANT_ID = "tenant_id";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_EMAIL = "email";

    public String generateAccessToken(UUID userId, UUID tenantId, String email, Set<String> roles) {
        return buildToken(userId, tenantId, email, roles, jwtExpiration, "access");
    }

    public String generateRefreshToken(UUID userId, UUID tenantId) {
        return buildToken(userId, tenantId, null, null, refreshTokenExpiration, "refresh");
    }

    private String buildToken(UUID userId, UUID tenantId, String email, Set<String> roles, long expiration, String tokenType) {
        long now = System.currentTimeMillis();
        long expiryTime = now + expiration;

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TENANT_ID, tenantId.toString());
        claims.put(CLAIM_EMAIL, email);
        claims.put(CLAIM_ROLES, roles != null ? new ArrayList<>(roles) : new ArrayList<>());
        claims.put("type", tokenType);

        SecretKey key = getSigningKey();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expiryTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public UUID extractUserId(String token) {
        Claims claims = getAllClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public UUID extractTenantId(String token) {
        Claims claims = getAllClaims(token);
        String tenantId = claims.get(CLAIM_TENANT_ID, String.class);
        return UUID.fromString(tenantId);
    }

    public String extractEmail(String token) {
        Claims claims = getAllClaims(token);
        return claims.get(CLAIM_EMAIL, String.class);
    }

    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        Claims claims = getAllClaims(token);
        List<String> rolesList = claims.get(CLAIM_ROLES, List.class);
        return rolesList != null ? new HashSet<>(rolesList) : new HashSet<>();
    }

    public String getTokenType(String token) {
        Claims claims = getAllClaims(token);
        return claims.get("type", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            log.warn("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getAllClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        }
    }

    public long getExpirationTime(String token) {
        Claims claims = getAllClaims(token);
        long expiryDate = claims.getExpiration().getTime();
        long now = System.currentTimeMillis();
        return (expiryDate - now) / 1000;  // in seconds
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
