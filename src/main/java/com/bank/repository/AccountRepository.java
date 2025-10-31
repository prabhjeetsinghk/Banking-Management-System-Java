package com.bank.repository;

import com.bank.model.Account;
import com.bank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUser(User user);

    // ✅ New method to fetch account by username
    Optional<Account> findByUserUsername(String username);
}
