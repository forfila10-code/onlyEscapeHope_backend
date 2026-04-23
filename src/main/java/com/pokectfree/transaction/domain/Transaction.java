package com.pokectfree.transaction.domain;

import com.pokectfree.login.domain.BaseTimeEntity;
import com.pokectfree.login.domain.User;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * 유저 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


     /**
     * 유저 이메일
     */
    @Column(nullable = false , length = 50)
    private String email;


    /**
     * 거래 유형: "INCOME"(수입) 또는 "EXPENSE"(지출)
     */
    @Column(nullable = false, length = 10)
    private String type;

    /**
     * 거래 날짜 (yyyy-MM-dd)
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * 금액 (원 단위)
     */
    @Column(nullable = false)
    private Long amount;

    /**
     * 카테고리 (예: 식비, 교통, 급여 등)
     */
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * 메모 (선택 입력)
     */
    @Column(length = 255)
    private String memo;

    @Builder
    public Transaction(User user, String email, String type, LocalDate date, Long amount, String category, String memo) {
        this.user     = user;   // ManyToOne 연관관계 설정
        this.email    = email;
        this.type     = type;
        this.date     = date;
        this.amount   = amount;
        this.category = category;
        this.memo     = memo;
    }
}
