package com.pokectfree.login.dto;

import com.pokectfree.login.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Spring Security의 OAuth2User를 구현한 커스텀 Principal.
 *
 * 목적: 인증 객체(Authentication)의 principal에 우리 DB의 User 엔티티를 담아두기 위해 사용합니다.
 * 이렇게 하면 컨트롤러에서 @AuthenticationPrincipal CustomOAuth2User 로
 * 로그인한 유저의 DB id, email 등을 바로 꺼낼 수 있습니다.
 */
public class CustomOAuth2User implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    /** 우리 DB에 저장된 User 엔티티를 반환합니다. */
    public User getUser() {
        return user;
    }

    /** 카카오에서 받아온 원본 attributes (id, kakao_account 등) */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /** 권한 목록: 현재는 모든 소셜 로그인 유저에게 ROLE_USER 부여 */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /** Principal의 이름 = 우리 DB의 email */
    @Override
    public String getName() {
        return user.getEmail();
    }
}
