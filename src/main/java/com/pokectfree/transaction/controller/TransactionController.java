package com.pokectfree.transaction.controller;

import com.pokectfree.login.dto.CustomOAuth2User;
import com.pokectfree.transaction.dto.HomeSummaryDto;
import com.pokectfree.transaction.dto.MonthlyStatisticsDto;
import com.pokectfree.transaction.dto.TransactionRequestDto;
import com.pokectfree.transaction.dto.TransactionResponseDto;
import com.pokectfree.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
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

    /**
     * 로그인 유저의 전체 거래 내역 조회
     *
     * GET /api/transactions
     * Header: Authorization: Bearer <token>
     *
     * 응답 예시:
     * HTTP 200 OK
     * [
     *   { "id": 2, "type": "INCOME",  "date": "2026-04-25", "amount": 3000000, "category": "급여", "memo": null },
     *   { "id": 1, "type": "EXPENSE", "date": "2026-04-01", "amount": 15000,   "category": "식비", "memo": "점심" }
     * ]
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponseDto>> getAllTransactions(
            @RequestParam(required = false) Long workspaceId,
            @AuthenticationPrincipal CustomOAuth2User principal) {

        Long userId = principal.getUser().getId();
        List<TransactionResponseDto> result = transactionService.getAllTransactions(userId, workspaceId);
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 연도·월의 거래 내역 목록 조회 (달력 대시보드용)
     *
     * GET /api/transactions/monthly?year=2026&month=4
     * Header: Authorization: Bearer <token>
     *
     * 응답 예시:
     * HTTP 200 OK
     * [
     *   { "id": 1, "type": "EXPENSE", "date": "2026-04-01", "amount": 15000, "category": "식비", "memo": "점심" },
     *   { "id": 2, "type": "INCOME",  "date": "2026-04-25", "amount": 3000000, "category": "급여", "memo": null }
     * ]
     *
     * @param year      조회 연도 (필수, 예: 2026)
     * @param month     조회 월   (필수, 예: 4)
     * @param principal JWT로 인증된 로그인 유저 정보
     */
    @GetMapping("/monthly")
    public ResponseEntity<List<TransactionResponseDto>> getMonthlyTransactions(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long workspaceId,
            @AuthenticationPrincipal CustomOAuth2User principal) {

        Long userId = principal.getUser().getId();

        List<TransactionResponseDto> result =
                transactionService.getMonthlyTransactions(userId, workspaceId, year, month);

        return ResponseEntity.ok(result);
    }

    /**
     * 메인페이지 홈 요약 조회
     *
     * GET /api/transactions/home-summary?year=2026&month=5&workspaceId=1
     *
     * @param year 조회 연도 (선택, 기본값: 현재 연도)
     * @param month 조회 월 (선택, 기본값: 현재 월)
     * @param workspaceId 조회할 워크스페이스 ID (선택, 기본값: 개인 워크스페이스)
     * @param principal JWT로 인증된 로그인 유저 정보
     * @return 이번 달 총 수입/지출/순액과 최근 거래 5건
     */
    @GetMapping("/home-summary")
    public ResponseEntity<HomeSummaryDto> getHomeSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long workspaceId,
            @AuthenticationPrincipal CustomOAuth2User principal) {

        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        Long userId = principal.getUser().getId();
        HomeSummaryDto result =
                transactionService.getHomeSummary(userId, workspaceId, targetYear, targetMonth);

        return ResponseEntity.ok(result);
    }

    /**
     * 특정 연도·월의 카테고리별 지출/수입 통계 조회 (차트 시각화용)
     *
     * year, month 파라미터 생략 시 현재 연월을 기본값으로 사용합니다.
     *
     * GET /api/transactions/statistics?year=2026&month=4
     * Header: Authorization: Bearer <token>
     *
     * 응답 예시:
     * HTTP 200 OK
     * {
     *   "year": 2026,
     *   "month": 4,
     *   "totalIncome": 3000000,
     *   "totalExpense": 230000,
     *   "netAmount": 2770000,
     *   "categoryBreakdown": [
     *     { "category": "급여",   "type": "INCOME",  "totalAmount": 3000000, "percent": 100.0 },
     *     { "category": "식비",   "type": "EXPENSE", "totalAmount": 150000,  "percent": 65.2  },
     *     { "category": "교통비", "type": "EXPENSE", "totalAmount": 80000,   "percent": 34.8  }
     *   ]
     * }
     *
     * @param year      조회 연도 (선택, 기본값: 현재 연도)
     * @param month     조회 월   (선택, 기본값: 현재 월)
     * @param principal JWT로 인증된 로그인 유저 정보
     */
    @GetMapping("/statistics")
    public ResponseEntity<MonthlyStatisticsDto> getMonthlyStatistics(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long workspaceId,
            @AuthenticationPrincipal CustomOAuth2User principal) {

        // year, month 미입력 시 현재 연월로 대체
        LocalDate now = LocalDate.now();
        int targetYear  = (year  != null) ? year  : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        Long userId = principal.getUser().getId();
        MonthlyStatisticsDto result =
                transactionService.getMonthlyStatistics(userId, workspaceId, targetYear, targetMonth);

        return ResponseEntity.ok(result);
    }
}
