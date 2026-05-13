package com.yian.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JWT 工具类
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 accessToken — 含 userId + roleCodes，有效期2小时，用于接口鉴权。
     */
    public String generateAccessToken(Long userId, String username, List<String> roleCodes) {
        return Jwts.builder()
                .claims(Map.of("userId", userId, "roleCodes", roleCodes))
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成 refreshToken — 含 type=refresh 标记，有效期7天，只用于刷新 accessToken，不能用于业务接口。
     */
    public String generateRefreshToken(Long userId, String username) {
        return Jwts.builder()
                .claims(Map.of("userId", userId, "type", "refresh"))
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /**
     * 判断 token 是否为 refreshToken — refreshToken 仅用于获取新的 token 对，不能访问业务接口。
     */
    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(parseToken(token).get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }
}
