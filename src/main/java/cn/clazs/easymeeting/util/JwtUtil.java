package cn.clazs.easymeeting.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    // JWT密钥（生产环境应从配置文件读取）
    private static final String SECRET = "easymeeting-jwt-secret-key-2026-8285380ac3d9b931838eed128f6654ac7b1f2fe3c7c6dd1f47c9730228ca8d34cfd85bd4926c59336f42f63f762f287a84f4476f578ae5d993";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    
    // Token有效期：7天（与 Redis 会话保持一致）
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 生成JWT Token
     */
    public static String generateToken(String userId) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + EXPIRE_TIME);
        
        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(KEY)
                .compact();
    }

    /**
     * 解析Token获取用户ID
     */
    public static String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * 验证Token是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
