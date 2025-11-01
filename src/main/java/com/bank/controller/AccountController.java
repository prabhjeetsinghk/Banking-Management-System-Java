package com.bank.controller;

import com.bank.model.Account;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import com.bank.security.JwtTokenUtil;
import com.bank.service.AccountService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    // Create account for logged-in user
    @PostMapping("/create")
    public ResponseEntity<Account> createAccount(Authentication authentication) {
        String username = authentication.getName();
        Account account = accountService.createAccount(username);
        return ResponseEntity.ok(account);
    }

    // Deposit to account
    @PostMapping("/deposit")
    public ResponseEntity<Account> deposit(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, Object> payload) {
        String token = authHeader.replace("Bearer ", ""); // remove "Bearer " prefix
        String accountNumber = jwtTokenUtil.getClaim(token, "accountNumber");
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        Account account = accountService.deposit(accountNumber, amount);
        return ResponseEntity.ok(account);
    }
    

    // Withdraw from account
    @PostMapping("/withdraw")
    public ResponseEntity<Account> withdraw(@RequestHeader("Authorization") String authHeader,@RequestBody Map<String, Object> payload) {
        // String accountNumber = (String) payload.get("accountNumber");
        String token = authHeader.replace("Bearer ", ""); // remove "Bearer " prefix
        String accountNumber = jwtTokenUtil.getClaim(token, "accountNumber");
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        Account account = accountService.withdraw(accountNumber, amount);
        return ResponseEntity.ok(account);
    }

    // Transfer between accounts
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestHeader("Authorization") String authHeader,@RequestBody Map<String, Object> payload) {

        String token = authHeader.replace("Bearer ", ""); // remove "Bearer " prefix
        String fromAccountNumber = jwtTokenUtil.getClaim(token, "accountNumber");



        String recipientEmail = (String) payload.get("email");
        // 3️⃣ Fetch recipient account number
        Account recipientAccount = accountRepository.findByUser(
                userRepository.findByEmail(recipientEmail)
                        .orElseThrow(() -> new RuntimeException("Recipient user not found")))
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Recipient account not found"));

        String toAccountNumber = recipientAccount.getAccountNumber();



        BigDecimal amount = new BigDecimal(payload.get("amount").toString());

        accountService.transfer(fromAccountNumber, toAccountNumber, amount);
        return ResponseEntity.ok("Transfer successful");
    }

    // List all accounts of logged-in user
    @GetMapping("/myaccounts")
    public ResponseEntity<List<Account>> myAccounts(Authentication authentication) {
        String username = authentication.getName();
        List<Account> accounts = accountService.getAccountsByUser(username);
        return ResponseEntity.ok(accounts);
    }
}