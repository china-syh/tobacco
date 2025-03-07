package com.example.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import java.util.Base64;

@Component  // ✅ 确保 Spring 管理这个类
public class JwtUtil {

//    private static final byte[] SECRET_KEY_BYTES = "MySuperSecretKey1234567890123456".getBytes(StandardCharsets.UTF_8);
//    private Key getSigningKey() {
//        return Keys.hmacShaKeyFor(SECRET_KEY_BYTES);  // ✅ 正确的密钥生成方式
//    }
@Value("${jwt.secret}")
private String secretKey;

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);  // 解码 Base64 编码的密钥
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString()) // 将 userId 转为 String
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))  // 1 小时有效期
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.out.println("Token 校验失败: " + e.getMessage());
            e.printStackTrace(); // 打印堆栈信息
            return false;
        }
    }

    // ✅ 提取用户名
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return (claims != null) ? claims.getSubject() : null;
    }

    // ✅ 提取所有 Claims
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            System.out.println("无法解析 Token: " + e.getMessage());
            return null;
        }
    }
}
