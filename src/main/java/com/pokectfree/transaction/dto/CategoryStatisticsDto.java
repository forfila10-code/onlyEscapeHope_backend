package com.pokectfree.transaction.dto;

import lombok.Getter;

/**
 * 카테고리별 거래 합계 DTO
 *
 * 예시 JSON:
 * { "category": "식비", "type": "EXPENSE", "totalAmount": 150000, "percent": 65.2 }
 */
@Getter
public class CategoryStatisticsDto {

    /** 카테고리명 (예: 식비, 교통, 급여 등) */
    private final String category;

    /** 거래 유형: "INCOME" 또는 "EXPENSE" */
    private final String type;

    /** 해당 카테고리의 합계 금액 (원 단위) */
    private final Long totalAmount;

    /** 같은 거래 유형(INCOME/EXPENSE) 총액 대비 비율 (%) */
    private final Double percent;

    public CategoryStatisticsDto(String category, String type, Long totalAmount, Double percent) {
        this.category    = category;
        this.type        = type;
        this.totalAmount = totalAmount;
        this.percent     = percent;
    }
}
