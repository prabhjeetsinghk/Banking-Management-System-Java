package com.bank.controller;

import com.bank.model.Account;
import com.bank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    // Create account for logged-in user
    @PostMapping("/create")
    public ResponseEntity<Account> createAccount(Authentication authentication) {
        String username = authentication.getName();
        Account account = accountService.createAccount(username);
        return ResponseEntity.ok(account);
    }

    // Deposit to account
    @PostMapping("/deposit")
    public ResponseEntity<Account> deposit(@RequestBody Map<String, Object> payload) {
        String accountNumber = (String) payload.get("accountNumber");
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        Account account = accountService.deposit(accountNumber, amount);
        return ResponseEntity.ok(account);
    }

    // Withdraw from account
    @PostMapping("/withdraw")
    public ResponseEntity<Account> withdraw(@RequestBody Map<String, Object> payload) {
        String accountNumber = (String) payload.get("accountNumber");
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        Account account = accountService.withdraw(accountNumber, amount);
        return ResponseEntity.ok(account);
    }

    // List all accounts of logged-in user
    @GetMapping("/myaccounts")
    public ResponseEntity<List<Account>> myAccounts(Authentication authentication) {
        String username = authentication.getName();
        List<Account> accounts = accountService.getAccountsByUser(username);
        return ResponseEntity.ok(accounts);
    }
}