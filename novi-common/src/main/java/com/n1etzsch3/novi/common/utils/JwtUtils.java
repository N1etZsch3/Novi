package com.n1etzsch3.novi.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${novi.jwt.secret-key}")
    private String secretKey;

    @Value("${novi.jwt.ttl}")
    private Long ttl;

    private SecretKey getSigningKey() {
        // 从 String 类型的秘钥生成 SecretKey 对象
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * 生成 JWT Token
     * @param userId 用户ID
     * @param username 用户名
     * @return Token 字符串
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ttl);

        return Jwts.builder()
                .claims(claims) // 设置自定义声明
                .subject(userId.toString()) // 主题，通常是用户ID
                .issuedAt(now) // 签发时间
                .expiration(expiryDate) // 过期时间
                .signWith(getSigningKey(), Jwts.SIG.HS256) // 签名算法
                .compact();
    }

    /**
     * 解析 JWT Token
     * @param token Token 字符串
     * @return Claims (
     *
     *
     * 包含 userId 和 username)
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // 使用秘钥验证
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 检查 Token 是否过期
     * @param claims Claims 对象
     * @return true 如果过期
     */
    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}