package com.bank.repository;

import com.bank.model.Transaction;
import com.bank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromAccount(Account account);
    List<Transaction> findByToAccount(Account account);
}
