package com.pokectfree.login.service; // 본인 패키지명 확인!

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.pokectfree.login.domain.User;
import com.pokectfree.login.dto.CustomOAuth2User;
import com.pokectfree.login.repository.UserRepository;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 카카오에서 유저 정보 받아오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "kakao"
        
        
        // 2. 카카오 데이터 파싱 (고유번호, 닉네임 추출)
        String providerId = oAuth2User.getAttribute("id").toString();
        Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = (String) profile.get("nickname");

        // 3. 💡 이메일 권한을 패스했으므로 가짜 이메일 강제 생성!
        String email = providerId + "@kakao.com";

        log.info("userRequest =============================== str ");
        log.info("oAuth2User : "  + oAuth2User);
        log.info("email : " + email);
        log.info("nickname : " + nickname);
        log.info("userRequest =============================== edn ");
        
        // 4. DB에 없으면 회원가입(Save), 있으면 그대로 통과
        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .nickname(nickname)
                            .provider(provider)
                            .providerId(providerId)
                            .build();
                    return userRepository.save(newUser); // 알아서 insert 쿼리 날림!
                });

        // 5. 우리 DB의 User 엔티티를 CustomOAuth2User로 감싸서 반환
        //    → 세션/SecurityContextHolder의 principal에 User.id가 포함됩니다.
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }
}