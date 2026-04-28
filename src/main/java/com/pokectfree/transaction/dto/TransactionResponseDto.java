package com.pokectfree.transaction.dto;

import com.pokectfree.transaction.domain.Transaction;
import lombok.Getter;

import java.time.LocalDate;

/**
 * GET /api/transactions/monthly 응답 시 사용하는 DTO
 *
 * 예시 JSON:
 * {
 *   "id": 1,
 *   "type": "EXPENSE",
 *   "date": "2026-04-23",
 *   "amount": 15000,
 *   "category": "식비",
 *   "memo": "점심 식사"
 * }
 */
@Getter
public class TransactionResponseDto {

    /** 거래 내역 PK */
    private final Long id;

    /** 거래 유형: "INCOME"(수입) 또는 "EXPENSE"(지출) */
    private final String type;

    /** 거래 날짜 (yyyy-MM-dd 형식) */
    private final LocalDate date;

    /** 금액 (원 단위) */
    private final Long amount;

    /** 카테고리 (예: 식비, 교통, 급여 등) */
    private final String category;

    /** 메모 (선택 입력, null 가능) */
    private final String memo;

    /**
     * Transaction 엔티티 → ResponseDto 변환 생성자
     */
    public TransactionResponseDto(Transaction transaction) {
        this.id       = transaction.getId();
        this.type     = transaction.getType();
        this.date     = transaction.getDate();
        this.amount   = transaction.getAmount();
        this.category = transaction.getCategory();
        this.memo     = transaction.getMemo();
    }
}
