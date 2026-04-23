package com.pokectfree.login.handler;

import com.pokectfree.login.dto.CustomOAuth2User;
import com.pokectfree.login.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 카카오 OAuth2 로그인 성공 후 실행되는 핸들러.
 *
 * 동작 순서:
 *   1. Authentication에서 CustomOAuth2User(= 우리 DB User 포함)를 꺼낸다.
 *   2. JwtTokenProvider로 JWT 토큰을 생성한다.
 *   3. 리액트 콜백 페이지로 토큰을 쿼리 파라미터에 담아 리다이렉트한다.
 *
 * 리액트 처리 예시 (React /oauth/callback 페이지):
 *   const token = new URLSearchParams(window.location.search).get('token');
 *   localStorage.setItem('accessToken', token);
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    /** application.yml → frontend.url */
    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
        Long   userId = principal.getUser().getId();
        String email  = principal.getUser().getEmail();

        String token = jwtTokenProvider.generateToken(userId, email);

        log.info("OAuth2 로그인 성공 - userId: {}, email: {}, JWT 발급 완료", userId, email);

        // 리액트 콜백 페이지로 토큰 전달
        // 예) http://localhost:5173/oauth/callback?token=eyJhbGci...
        response.sendRedirect(frontendUrl + "/oauth/callback?token=" + token);
    }
}
