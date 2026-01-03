// src/main/java/com/example/bank/repository/TransactionRepository.java
package com.example.bank.repository;

import com.example.bank.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
            "AND (:from IS NULL OR t.createdAt >= :from) " +
            "AND (:to IS NULL OR t.createdAt <= :to) " +
            "ORDER BY t.createdAt DESC")
    List<Transaction> findTransactionsByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
