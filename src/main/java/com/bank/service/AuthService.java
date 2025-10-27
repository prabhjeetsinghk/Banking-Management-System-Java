package com.bank.service;

import com.bank.model.User;
import com.bank.repository.UserRepository;
import com.bank.security.JwtTokenUtil;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    // --- Register ---
    public String register(String username, String email, String password, boolean enable2FA) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
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

        userRepository.save(user);
        return enable2FA ? user.getTwoFactorSecret() : "Registered successfully (2FA disabled)";
    }

    // --- Login ---
    public String login(String username, String password, Integer otp) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty())
            throw new RuntimeException("Invalid username/password");

        User user = optionalUser.get();

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new RuntimeException("Invalid username/password");

        // Handle 2FA
        if (user.isTwoFactorEnabled()) {
            if (otp == null)
                throw new RuntimeException("OTP required for 2FA-enabled account");
            if (!gAuth.authorize(user.getTwoFactorSecret(), otp))
                throw new RuntimeException("Invalid OTP");
        }

        // Generate JWT token
        return jwtTokenUtil.generateToken(username);
    }
}
