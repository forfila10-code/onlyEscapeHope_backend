package com.pokectfree.transaction.domain;

import com.pokectfree.login.domain.BaseTimeEntity;
import com.pokectfree.login.domain.User;
import com.pokectfree.workspace.domain.Workspace;

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
     * 거래를 작성한 유저 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 거래가 속한 워크스페이스
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    /**
     * 실제 결제자. 공유 워크스페이스에서는 작성자와 결제자가 다를 수 있습니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by")
    private User paidBy;


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

    /**
     * @param user 거래를 작성한 유저
     * @param workspace 거래가 속한 워크스페이스
     * @param paidBy 실제 결제자로 기록할 유저
     * @param email 작성자 이메일
     * @param type 거래 유형(INCOME/EXPENSE)
     * @param date 거래 날짜
     * @param amount 거래 금액
     * @param category 거래 카테고리
     * @param memo 거래 메모
     */
    @Builder
    public Transaction(User user, Workspace workspace, User paidBy, String email, String type, LocalDate date, Long amount, String category, String memo) {
        this.user      = user;   // 거래 작성자
        this.workspace = workspace;
        this.paidBy    = paidBy;
        this.email     = email;
        this.type      = type;
        this.date      = date;
        this.amount    = amount;
        this.category  = category;
        this.memo      = memo;
    }
}
