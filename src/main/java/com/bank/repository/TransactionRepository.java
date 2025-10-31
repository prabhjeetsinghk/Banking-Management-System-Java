package com.bank.repository;

import com.bank.model.Transaction;
import com.bank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Find all transactions for a given account
    // List<Transaction> findByAccount(Account account);
    
    // ✅ find transactions where the account is the sender
    List<Transaction> findByFromAccount(Account fromAccount);

    // ✅ find transactions where the account is the receiver
    List<Transaction> findByToAccount(Account toAccount);
}
