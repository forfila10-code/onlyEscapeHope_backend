package com.pokectfree.transaction.dto;

import com.pokectfree.login.domain.User;
import com.pokectfree.transaction.domain.Transaction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 프론트엔드에서 POST /api/transactions 요청 시 전송하는 JSON 바디를 매핑하는 DTO
 *
 * 예시 JSON:
 * {
 *   "type": "EXPENSE",
 *   "date": "2026-04-23",
 *   "amount": 15000,
 *   "category": "식비",
 *   "memo": "점심 식사"
 * }
 */
@Getter
@Setter
@NoArgsConstructor
public class TransactionRequestDto {

    /** 거래 유형: "INCOME"(수입) 또는 "EXPENSE"(지출) */
    private String type;

    /** 거래 날짜 (yyyy-MM-dd 형식) */
    private LocalDate date;

    /** 금액 (원 단위) */
    private Long amount;

    /** 카테고리 (예: 식비, 교통, 급여 등) */
    private String category;

    /** 메모 (선택 입력) */
    private String memo;

    /**
     * DTO → Entity 변환 메서드
     *
     * @param user JWT에서 추출한 userId로 DB에서 조회한 User 엔티티
     *             → Transaction.user(FK) 와 Transaction.email 동시 세팅
     */
    public Transaction toEntity(User user) {
        return Transaction.builder()
                .user(user)            // ManyToOne FK 연결
                .email(user.getEmail()) // 이메일 컬럼 (빠른 조회용 비정규화)
                .type(this.type)
                .date(this.date)
                .amount(this.amount)
                .category(this.category)
                .memo(this.memo)
                .build();
    }
}
