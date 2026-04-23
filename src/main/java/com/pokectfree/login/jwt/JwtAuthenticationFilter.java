package com.pokectfree.login.jwt;

import com.pokectfree.login.dto.CustomOAuth2User;
import com.pokectfree.login.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * 매 HTTP 요청마다 한 번만 실행되는 JWT 인증 필터.
 *
 * 처리 순서:
 *   1. Authorization 헤더에서 "Bearer <token>" 추출
 *   2. JwtTokenProvider로 토큰 검증 (서명 + 만료 체크)
 *   3. 토큰에서 userId 파싱 → DB에서 User 조회
 *   4. CustomOAuth2User를 Principal로 하는 Authentication 객체 생성
 *   5. SecurityContextHolder에 등록 → 이후 컨트롤러에서 @AuthenticationPrincipal로 꺼낼 수 있음
 *
 * 리액트에서 API 요청 시 헤더 형식:
 *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository   userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserId(token);

            userRepository.findById(userId).ifPresentOrElse(
                user -> {
                    // Principal에 우리 User 엔티티를 담은 CustomOAuth2User 생성
                    CustomOAuth2User principal = new CustomOAuth2User(user, Map.of());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,                        // credentials (JWT 방식에선 null)
                                    principal.getAuthorities()   // [ROLE_USER]
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT 인증 성공 - userId: {}, uri: {}", userId, request.getRequestURI());
                },
                () -> log.warn("JWT에 담긴 userId={}가 DB에 존재하지 않음", userId)
            );
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출.
     * "Bearer " 접두어를 제거한 순수 JWT 문자열 반환.
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
