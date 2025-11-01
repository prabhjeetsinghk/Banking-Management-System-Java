package com.bank.service;

import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import com.bank.security.JwtTokenUtil;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenUtil jwtTokenUtil, AccountService accountService, TransactionRepository transactionRepository, 
            AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.transactionRepository = transactionRepository;
    }

    // --- Register ---
    public String register(String username, String email, String password, boolean enable2FA) {
        log.info("Registering new user: {}", username);
        
        if (userRepository.findByUsername(username).isPresent()) {
            log.warn("Registration failed — username {} already exists", username);            
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setTwoFactorEnabled(enable2FA);

        if (enable2FA) {
            GoogleAuthenticatorKey key = gAuth.createCredentials();
            user.setTwoFactorSecret(key.getKey());
        }

        User savedUser = userRepository.save(user);

        // Create account record
        Account account = accountService.createAccount(savedUser.getUsername());

        // Create tx record
        Transaction txn = new Transaction();
        txn.setFromAccount(null);
        txn.setToAccount(account);
        txn.setAmount(BigDecimal.ZERO);
        txn.setType("ACCOUNT_CREATION");
        txn.setTransactionDate(LocalDateTime.now());
        transactionRepository.save(txn);

        log.info("User {} registered successfully", username);
        
        return enable2FA
                ? "2FA enabled. Please scan the QR code with your authenticator app."
                : "Registered successfully (2FA disabled)";
    }

    // --- Login ---
    public String login(String username, String password, Integer otp) {
        log.info("User attempting login: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username/password"));

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username/password");

        if (user.isTwoFactorEnabled()) {
            if (otp == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP required for 2FA-enabled account");
            if (!gAuth.authorize(user.getTwoFactorSecret(), otp))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        List<Account> accounts = accountRepository.findByUser(user);
        if (accounts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No account found for user");
        }

        String accountNumber = accounts.get(0).getAccountNumber();

        return jwtTokenUtil.generateToken(username, accountNumber);
    }
}
