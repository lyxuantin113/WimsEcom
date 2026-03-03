package com.wims.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // 1. Secret Key: Chìa khóa bí mật để ký tên lên Token.
    @Value("${jwt.secretkey}")
    private String JWT_SECRET;

    @Value("${jwt.expiration}")
    private long ACCESS_TOKEN_EXPIRATION;

    @Value("${jwt.refresh-expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }

    // TẠO ACCESS TOKEN
    public String generateAccessToken(String username) {
        return generateToken(username, ACCESS_TOKEN_EXPIRATION);
    }

    // TẠO REFRESH TOKEN
    public String generateRefreshToken(String username) {
        return generateToken(username, REFRESH_TOKEN_EXPIRATION);
    }

    private String generateToken(String username, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // LẤY USERNAME TỪ TOKEN
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // LẤY NGÀY HẾT HẠN TỪ TOKEN
    public Date getExpirationFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    // VALIDATE TOKEN
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            logError("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logError("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logError("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logError("JWT claims string is empty.");
        }
        return false;
    }

    private void logError(String message) {
        System.err.println(message);
    }
}