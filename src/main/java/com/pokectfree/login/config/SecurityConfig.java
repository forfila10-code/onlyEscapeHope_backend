package com.pokectfree.login.config;

import com.pokectfree.login.handler.OAuth2LoginSuccessHandler;
import com.pokectfree.login.jwt.JwtAuthenticationFilter;
import com.pokectfree.login.jwt.JwtTokenProvider;
import com.pokectfree.login.repository.UserRepository;
import com.pokectfree.login.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 (JWT 기반 Stateless 방식)
 *
 * ┌─────────────────────────────────────────────────────────┐
 * │  [React:5173]  →  카카오 OAuth2 로그인                    │
 * │      ↓                                                  │
 * │  [Spring]  →  JWT 발급  →  /oauth/callback?token=xxx    │
 * │      ↓                                                  │
 * │  [React]  →  localStorage.setItem('accessToken', token) │
 * │      ↓                                                  │
 * │  [React]  →  API 요청 시 Authorization: Bearer <token>  │
 * │      ↓                                                  │
 * │  JwtAuthenticationFilter  →  토큰 검증  →  Controller   │
 * └─────────────────────────────────────────────────────────┘
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService    customOAuth2UserService;
    private final OAuth2LoginSuccessHandler  oAuth2LoginSuccessHandler;
    private final JwtTokenProvider           jwtTokenProvider;
    private final UserRepository             userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ① CSRF 비활성화 (JWT 사용 시 불필요)
            .csrf(csrf -> csrf.disable())

            // ② CORS: WebConfig(WebMvcConfigurer)의 설정을 그대로 적용
            .cors(Customizer.withDefaults())

            // ③ 세션 비활성화 → JWT로만 인증 (Stateless)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ④ URL 접근 권한
            .authorizeHttpRequests(auth -> auth
                // 카카오 OAuth2 흐름 엔드포인트는 인증 없이 허용
                .requestMatchers("/", "/oauth2/**", "/login/**").permitAll()
                // /error 는 스프링 내부 에러 처리 경로 → 시큐리티가 401로 가로채지 않도록 허용
                .requestMatchers("/error").permitAll()
                // /api/** 는 반드시 JWT 인증 필요
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )

            // ⑤ OAuth2 소셜 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    // 카카오 유저 정보를 CustomOAuth2User로 매핑
                    .userService(customOAuth2UserService)
                )
                // 로그인 성공 → JWT 발급 → 리액트 /oauth/callback?token=xxx 리다이렉트
                .successHandler(oAuth2LoginSuccessHandler)
            )

            // ⑥ 인증 실패 시 401 Unauthorized 반환 (프론트엔드 axios interceptor 연동용)
            //    - 토큰 없음 / 만료 / 유효하지 않은 토큰 → 401 + "Unauthorized" 텍스트
            //    - 프론트에서 401 수신 시 로그인 페이지로 리다이렉트 처리
            .exceptionHandling(e -> e
                .authenticationEntryPoint((request, response, ex) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("text/plain;charset=UTF-8");
                    response.getWriter().write("Unauthorized");
                })
            )

            // ⑦ JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
            //    → 모든 요청에서 Authorization 헤더를 먼저 검증
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider, userRepository),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
