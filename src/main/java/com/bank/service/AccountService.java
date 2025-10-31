package com.bank.service;

import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    // Create new account for a user
    public Account createAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setBalance(BigDecimal.ZERO);

        return accountRepository.save(account);
    }

    // Deposit money
    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Update balance
        account.setBalance(account.getBalance().add(amount));
        Account savedAccount = accountRepository.save(account);

        // Log transaction
        Transaction txn = new Transaction();
        txn.setFromAccount(null);
        txn.setToAccount(account);
        txn.setType("DEPOSIT");
        txn.setAmount(amount);
        txn.setTransactionDate(LocalDateTime.now());
        transactionRepository.save(txn);

        return savedAccount;
    }

    // Withdraw money
    @Transactional
    public Account withdraw(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Update balance
        account.setBalance(account.getBalance().subtract(amount));
        Account savedAccount = accountRepository.save(account);

        // Log transaction
        Transaction txn = new Transaction();
        txn.setFromAccount(account);
        txn.setToAccount(null);
        txn.setType("WITHDRAWAL");
        txn.setAmount(amount);
        txn.setTransactionDate(LocalDateTime.now());
        transactionRepository.save(txn);

        return savedAccount;
    }

    // Transfer money between accounts
    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new RuntimeException("Sender account not found"));

        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in sender account");
        }

        // Update balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Log transactions
        Transaction outTxn = new Transaction();
        outTxn.setFromAccount(fromAccount);
        outTxn.setToAccount(toAccount);
        outTxn.setType("TRANSFER_OUT");
        outTxn.setAmount(amount);
        outTxn.setTransactionDate(LocalDateTime.now());
        transactionRepository.save(outTxn);

        Transaction inTxn = new Transaction();
        inTxn.setFromAccount(fromAccount);
        inTxn.setToAccount(toAccount);
        inTxn.setType("TRANSFER_IN");
        inTxn.setAmount(amount);
        inTxn.setTransactionDate(LocalDateTime.now());
        transactionRepository.save(inTxn);
    }

    // List all accounts for a user
    public List<Account> getAccountsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return accountRepository.findByUser(user);
    }

    // Generate unique account number
    private String generateUniqueAccountNumber() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
