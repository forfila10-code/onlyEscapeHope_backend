package com.pokectfree.transaction.dto;

import java.util.List;
import lombok.Getter;

/**
 * 메인페이지 홈 카드와 최근 내역을 한 번에 그리기 위한 응답 DTO
 */
@Getter
public class HomeSummaryDto {

    /** 조회 연도 */
    private final int year;

    /** 조회 월 */
    private final int month;

    /** 이번 달 총 수입 */
    private final Long totalIncome;

    /** 이번 달 총 지출 */
    private final Long totalExpense;

    /** 이번 달 순액 = 총 수입 - 총 지출 */
    private final Long netAmount;

    /** 선택된 워크스페이스 기준 최근 거래 내역 */
    private final List<TransactionResponseDto> recentTransactions;

    /**
     * @param year 조회 연도
     * @param month 조회 월
     * @param totalIncome 이번 달 총 수입
     * @param totalExpense 이번 달 총 지출
     * @param recentTransactions 선택된 워크스페이스의 최근 거래 내역
     */
    public HomeSummaryDto(int year,
                          int month,
                          Long totalIncome,
                          Long totalExpense,
                          List<TransactionResponseDto> recentTransactions) {
        this.year = year;
        this.month = month;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netAmount = totalIncome - totalExpense;
        this.recentTransactions = recentTransactions;
    }
}
