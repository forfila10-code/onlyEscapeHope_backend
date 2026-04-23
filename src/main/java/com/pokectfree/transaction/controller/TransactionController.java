package com.pokectfree.transaction.controller;

import com.pokectfree.login.dto.CustomOAuth2User;
import com.pokectfree.transaction.dto.TransactionRequestDto;
import com.pokectfree.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 가계부 거래 내역 REST API 컨트롤러
 *
 * 인증 방식: JWT Bearer Token
 *   - 리액트에서 API 호출 시 반드시 헤더에 포함:
 *     Authorization: Bearer <access_token>
 *
 * JwtAuthenticationFilter가 토큰을 검증하고 SecurityContextHolder에
 * Authentication(principal=CustomOAuth2User)을 등록하므로,
 * @AuthenticationPrincipal 로 로그인 유저 정보를 바로 꺼낼 수 있습니다.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 새로운 거래 내역 저장
     *
     * POST /api/transactions
     * Header: Authorization: Bearer <token>
     *
     * 요청 Body 예시:
     * {
     *   "type": "EXPENSE",
     *   "date": "2026-04-23",
     *   "amount": 15000,
     *   "category": "식비",
     *   "memo": "점심 식사"
     * }
     *
     * 응답 예시:
     * HTTP 201 Created
     * { "id": 1 }
     */
    @PostMapping
    public ResponseEntity<Map<String, Long>> save(
            @RequestBody TransactionRequestDto requestDto,
            @AuthenticationPrincipal CustomOAuth2User principal) {

        // JWT → JwtAuthenticationFilter → SecurityContextHolder → principal
        // principal에서 DB User의 PK(id)를 안전하게 추출
        Long userId = principal.getUser().getId();

        Long savedId = transactionService.save(requestDto, userId);

        return ResponseEntity
                .status(201)
                .body(Map.of("id", savedId));
    }
}
