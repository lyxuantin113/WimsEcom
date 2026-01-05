package com.wims.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // 1. Secret Key: Chìa khóa bí mật để ký tên lên Token.
    // Trong thực tế phải để trong application.yaml và cực kỳ phức tạp.
    // Ở đây demo mình để cứng một chuỗi dài (ít nhất 64 ký tự).
    private final String JWT_SECRET = "DayLaBiMatCuaWIMSBackendDungChoAiBietNheChuoiNayPhaiRatDaiMoiDuoc";

    // 2. Thời gian hết hạn của Token (Ví dụ: 1 ngày = 86400000 ms)
    private final long JWT_EXPIRATION = 86400000L;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }

    // TẠO TOKEN TỪ USERNAME
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);

        return Jwts.builder()
                .setSubject(username) // Lưu username vào token
                .setIssuedAt(now) // Ngày tạo
                .setExpiration(expiryDate) // Ngày hết hạn
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // Ký tên bằng thuật toán HS512
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

    // VALIDATE TOKEN (Kiểm tra xem token có phải hàng pha-ke hay hết hạn không)
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            System.err.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            System.err.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            System.err.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT claims string is empty.");
        }
        return false;
    }
}