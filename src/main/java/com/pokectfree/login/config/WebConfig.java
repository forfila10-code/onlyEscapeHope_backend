package com.pokectfree.login.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 전역 CORS 설정.
 *
 * JWT 방식에서는 쿠키 대신 Authorization 헤더를 사용하므로
 * allowCredentials(true) 대신 allowedHeaders에 "Authorization"을 명시합니다.
 *
 * application.yml의 cors.allowed-origins 값을 수정해 허용할 Origin을 관리하세요.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // JWT 인증에 필요한 Authorization 헤더를 명시적으로 허용
                .allowedHeaders("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With")
                // 리액트에서 응답 헤더를 읽을 수 있도록 노출
                .exposedHeaders("Authorization")
                .maxAge(3600); // preflight 캐시 1시간
    }
}
