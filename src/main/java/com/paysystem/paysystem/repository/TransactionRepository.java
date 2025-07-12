
package com.paysystem.paysystem.repository;

import com.paysystem.paysystem.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
