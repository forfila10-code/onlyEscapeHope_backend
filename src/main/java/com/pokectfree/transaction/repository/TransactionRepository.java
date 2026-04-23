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
}
