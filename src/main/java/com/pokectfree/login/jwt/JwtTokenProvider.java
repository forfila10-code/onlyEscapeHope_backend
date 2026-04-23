package com.pokectfree.login.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 토큰 생성 / 검증 / 파싱 담당 컴포넌트.
 *
 * application.yml에서 주입받는 설정:
 *   jwt.secret       : Base64 인코딩된 HMAC-SHA256 서명 키 (256bit 이상)
 *   jwt.expiration-ms: 토큰 유효 시간 (밀리초 단위, 기본 86400000 = 24h)
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        // Base64 디코딩 후 HMAC-SHA 키로 변환
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    // ────────────────────────────────────────────────
    // 토큰 생성
    // ────────────────────────────────────────────────

    /**
     * 카카오 OAuth2 로그인 성공 후 호출.
     * subject = userId(DB PK), claim "email" 포함.
     */
    public String generateToken(Long userId, String email) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))   // sub: "1"
                .claim("email", email)             // custom claim
                .issuedAt(now)                     // iat
                .expiration(expiry)                // exp
                .signWith(secretKey)               // HS256
                .compact();
    }

    // ────────────────────────────────────────────────
    // 토큰 검증
    // ────────────────────────────────────────────────

    /**
     * 서명 검증 + 만료 여부 확인.
     * 유효하면 true, 위변조·만료·null이면 false.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // ────────────────────────────────────────────────
    // 토큰 파싱
    // ────────────────────────────────────────────────

    /** 토큰의 subject(= userId)를 Long으로 반환 */
    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    /** 토큰의 email claim 반환 */
    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    // ────────────────────────────────────────────────
    // 내부 헬퍼
    // ────────────────────────────────────────────────

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
