package com.pokectfree.transaction.repository;

import com.pokectfree.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Transaction Entity에 대한 JPA Repository
 * JpaRepository 상속만으로 save, findById, findAll, delete 등 기본 CRUD 자동 제공
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * 특정 날짜의 모든 거래 내역 조회 (향후 확장용)
     */
    List<Transaction> findByDate(LocalDate date);

    /**
     * 특정 거래 유형(INCOME/EXPENSE)의 모든 내역 조회 (향후 확장용)
     */
    List<Transaction> findByType(String type);

    /**
     * 특정 유저의 날짜 범위(startDate ~ endDate) 내 거래 내역을 날짜 오름차순으로 조회
     *
     * Service 레이어에서 해당 월의 첫째 날(startDate)과 마지막 날(endDate)을 계산해서 전달합니다.
     * LocalDate.of(year, month, 1) ~ YearMonth.of(year, month).atEndOfMonth()
     *
     * - JPQL YEAR()/MONTH() 함수 없이 순수 날짜 범위로 필터링하므로 DB 인덱스 활용 가능
     * - Spring Data JPA 메서드 네이밍 규칙: user.id → User_Id 로 연관 엔티티 탐색
     *
     * @param userId    로그인 유저의 DB PK
     * @param startDate 조회 시작일 (해당 월의 1일, inclusive)
     * @param endDate   조회 종료일 (해당 월의 말일, inclusive)
     * @return 해당 월의 거래 내역 리스트 (날짜 오름차순)
     */
    List<Transaction> findByUser_IdAndDateBetweenOrderByDateAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * 특정 유저의 전체 거래 내역을 날짜 내림차순으로 조회 (최신 순)
     *
     * @param userId 로그인 유저의 DB PK
     * @return 전체 거래 내역 리스트 (날짜 내림차순)
     */
    List<Transaction> findByUser_IdOrderByDateDesc(Long userId);
}
