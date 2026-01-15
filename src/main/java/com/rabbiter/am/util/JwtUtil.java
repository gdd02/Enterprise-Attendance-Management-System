package com.rabbiter.am.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和验证JWT Token
 */
@Component
public class JwtUtil {

    // JWT密钥 (使用足够长的密钥以满足HS256要求)
    private static final String SECRET_KEY = "AttendanceManagerSystemSecretKeyForJWT2024VerySafeAndSecure";
    
    // Token有效期：24小时 (单位：毫秒)
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    // 生成密钥
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    /**
     * 生成JWT Token
     * @param employeeNumber 员工工号
     * @param employeeName 员工姓名
     * @return JWT Token字符串
     */
    public String generateToken(String employeeNumber, String employeeName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("number", employeeNumber);
        claims.put("name", employeeName);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(employeeNumber)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从Token中提取Claims
     * @param token JWT Token
     * @return Claims对象
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中获取员工工号
     * @param token JWT Token
     * @return 员工工号
     */
    public String getEmployeeNumberFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("number", String.class) : null;
    }

    /**
     * 从Token中获取员工姓名
     * @param token JWT Token
     * @return 员工姓名
     */
    public String getEmployeeNameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("name", String.class) : null;
    }

    /**
     * 验证Token是否有效
     * @param token JWT Token
     * @return true表示有效，false表示无效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return false;
            }
            // 检查Token是否过期
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查Token是否过期
     * @param token JWT Token
     * @return true表示过期，false表示未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return true;
            }
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 刷新Token
     * @param token 旧的JWT Token
     * @return 新的JWT Token
     */
    public String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            String number = claims.get("number", String.class);
            String name = claims.get("name", String.class);
            return generateToken(number, name);
        }
        return null;
    }
}

