package com.ragapp.auth;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Issues and validates JWTs. Tokens are self-contained: the signature and
 * expiry are the only trust anchors, so both registered organization users and
 * anonymous individual sessions can be authenticated without a user store.
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    /**
     * Mints a token for the given subject, carrying the data-isolation scope and role.
     *
     * @param subject     login username or a generated guest id
     * @param scopeKey    tenant boundary — {@code org:<orgId>} or {@code ind:<workspaceId>}
     * @param role        {@code ADMIN}, {@code ORG} or {@code INDIVIDUAL}
     * @param displayName human-friendly name shown in the UI
     */
    public String generateToken(String subject, String scopeKey, String role, String displayName) {
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .claim("scopeKey", scopeKey)
                .claim("role", role)
                .claim("name", displayName)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /** Parses and verifies a token, throwing if the signature is invalid or it has expired. */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public String extractScopeKey(String token) {
        return claimAsString(parse(token), "scopeKey");
    }

    public String extractRole(String token) {
        return claimAsString(parse(token), "role");
    }

    public String extractDisplayName(String token) {
        return claimAsString(parse(token), "name");
    }

    private static String claimAsString(Claims claims, String name) {
        Object value = claims.get(name);
        return value == null ? null : value.toString();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
