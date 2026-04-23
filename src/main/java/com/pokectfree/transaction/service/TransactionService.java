package com.pokectfree.transaction.service;

import com.pokectfree.login.domain.User;
import com.pokectfree.login.repository.UserRepository;
import com.pokectfree.transaction.domain.Transaction;
import com.pokectfree.transaction.dto.TransactionRequestDto;
import com.pokectfree.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 가계부 거래 내역 비즈니스 로직 서비스
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository        userRepository;

    /**
     * 새로운 거래 내역을 저장하고, 저장된 항목의 ID를 반환합니다.
     *
     * @param requestDto 프론트엔드에서 전달받은 거래 데이터
     * @param userId     JwtAuthenticationFilter → @AuthenticationPrincipal 로 넘어온 로그인 유저의 DB PK
     * @return 저장된 거래 내역의 ID
     */
    @Transactional
    public Long save(TransactionRequestDto requestDto, Long userId) {
        // JWT에서 추출한 userId로 실제 User 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 유저가 존재하지 않습니다. userId=" + userId));

        Transaction transaction = requestDto.toEntity(user);
        Transaction saved = transactionRepository.save(transaction);
        return saved.getId();
    }
}
