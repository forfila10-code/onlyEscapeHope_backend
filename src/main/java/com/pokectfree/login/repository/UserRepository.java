package com.pokectfree.login.repository;

import com.pokectfree.login.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository를 상속받으면, 웬만한 기본 DB 쿼리(저장, 조회, 삭제)는 스프링이 다 알아서 만들어줍니다!
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 카카오 로그인 시, 이미 가입된 유저인지 확인하기 위한 커스텀 검색기
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    Optional<User> findByEmail(String email);
}