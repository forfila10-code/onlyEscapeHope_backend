package com.pokectfree.transaction.service;

import com.pokectfree.login.domain.User;
import com.pokectfree.login.repository.UserRepository;
import com.pokectfree.transaction.domain.Transaction;
import com.pokectfree.transaction.dto.CategoryStatisticsDto;
import com.pokectfree.transaction.dto.MonthlyStatisticsDto;
import com.pokectfree.transaction.dto.TransactionRequestDto;
import com.pokectfree.transaction.dto.TransactionResponseDto;
import com.pokectfree.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * 로그인 유저의 전체 거래 내역을 최신 날짜 순으로 조회합니다.
     *
     * @param userId 로그인 유저의 DB PK
     * @return 전체 거래 내역 DTO 리스트 (날짜 내림차순)
     */
    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getAllTransactions(Long userId) {
        return transactionRepository.findByUser_IdOrderByDateDesc(userId)
                .stream()
                .map(TransactionResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 특정 연도·월의 거래 내역 목록을 조회합니다.
     *
     * LocalDate 범위(해당 월 1일 ~ 말일)로 필터링하므로 DB 인덱스를 그대로 활용합니다.
     *
     * @param userId 로그인 유저의 DB PK
     * @param year   조회 연도 (예: 2026)
     * @param month  조회 월   (예: 4)
     * @return 해당 월의 거래 내역 DTO 리스트 (날짜 오름차순)
     */
    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getMonthlyTransactions(Long userId, int year, int month) {
        // 해당 월의 첫째 날 ~ 말일 계산
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate   = yearMonth.atEndOfMonth();

        List<Transaction> transactions =
                transactionRepository.findByUser_IdAndDateBetweenOrderByDateAsc(userId, startDate, endDate);

        return transactions.stream()
                .map(TransactionResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 특정 연도·월의 카테고리별 통계를 계산합니다.
     *
     * 처리 흐름:
     *   1. 해당 월 거래 내역 전체 조회 (기존 월별 쿼리 재사용)
     *   2. Java Stream으로 (category, type) 기준 그룹화 후 금액 합산
     *   3. 총 수입 / 총 지출 기준으로 카테고리별 퍼센트 계산
     *   4. 총 수입 / 총 지출 / 카테고리별 합계 → MonthlyStatisticsDto 반환
     *
     * @param userId 로그인 유저의 DB PK
     * @param year   조회 연도 (예: 2026)
     * @param month  조회 월   (예: 4)
     * @return 월별 통계 DTO (총 수입, 총 지출, 순액, 카테고리별 합계)
     */
    @Transactional(readOnly = true)
    public MonthlyStatisticsDto getMonthlyStatistics(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate   = yearMonth.atEndOfMonth();

        List<Transaction> transactions =
                transactionRepository.findByUser_IdAndDateBetweenOrderByDateAsc(userId, startDate, endDate);

        // 총 수입 / 총 지출 합산
        long totalIncome = transactions.stream()
                .filter(t -> "INCOME".equals(t.getType()))
                .mapToLong(Transaction::getAmount)
                .sum();

        long totalExpense = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .mapToLong(Transaction::getAmount)
                .sum();

        // (category + type) 복합 키로 그룹화 → 금액 합산
        // 예: "식비|EXPENSE" → 150000L
        Map<String, Long> grouped = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() + "|" + t.getType(),
                        Collectors.summingLong(Transaction::getAmount)
                ));

        // Map → CategoryStatisticsDto 리스트 변환 (금액 내림차순)
        List<CategoryStatisticsDto> categoryBreakdown = grouped.entrySet().stream()
                .map(e -> {
                    String[] parts = e.getKey().split("\\|", 2);
                    String category = parts[0];
                    String type = parts[1];
                    long amount = e.getValue();
                    long typeTotal = "INCOME".equals(type) ? totalIncome : totalExpense;
                    double percent = typeTotal > 0 ? (amount * 100.0) / typeTotal : 0.0;

                    return new CategoryStatisticsDto(category, type, amount, percent);
                })
                .sorted(Comparator.comparingLong(CategoryStatisticsDto::getTotalAmount).reversed())
                .collect(Collectors.toList());

        return new MonthlyStatisticsDto(year, month, totalIncome, totalExpense, categoryBreakdown);
    }
}
