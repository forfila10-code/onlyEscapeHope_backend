package com.pokectfree.transaction.dto;

import lombok.Getter;

import java.util.List;

/**
 * GET /api/transactions/statistics 응답 DTO
 *
 * 예시 JSON:
 * {
 *   "year": 2026,
 *   "month": 4,
 *   "totalIncome": 3000000,
 *   "totalExpense": 230000,
 *   "netAmount": 2770000,
 *   "categoryBreakdown": [
 *     { "category": "급여",  "type": "INCOME",  "totalAmount": 3000000, "percent": 100.0 },
 *     { "category": "식비",  "type": "EXPENSE", "totalAmount": 150000,  "percent": 65.2  },
 *     { "category": "교통비","type": "EXPENSE", "totalAmount": 80000,   "percent": 34.8  }
 *   ]
 * }
 */
@Getter
public class MonthlyStatisticsDto {

    /** 조회 연도 */
    private final int year;

    /** 조회 월 */
    private final int month;

    /** 이번 달 총 수입 (INCOME 합계) */
    private final Long totalIncome;

    /** 이번 달 총 지출 (EXPENSE 합계) */
    private final Long totalExpense;

    /** 순액 = 총 수입 - 총 지출 */
    private final Long netAmount;

    /** 카테고리별 합계 리스트 (금액 내림차순) */
    private final List<CategoryStatisticsDto> categoryBreakdown;

    public MonthlyStatisticsDto(int year,
                                int month,
                                Long totalIncome,
                                Long totalExpense,
                                List<CategoryStatisticsDto> categoryBreakdown) {
        this.year              = year;
        this.month             = month;
        this.totalIncome       = totalIncome;
        this.totalExpense      = totalExpense;
        this.netAmount         = totalIncome - totalExpense;
        this.categoryBreakdown = categoryBreakdown;
    }
}
