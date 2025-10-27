package com.bank.service;

import com.bank.model.User;
import com.bank.repository.UserRepository;
import com.bank.security.JwtTokenUtil;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    // --- Register ---
    public String register(String username, String email, String password, boolean enable2FA) {
        if (userRepository.findByUsername(username).isPresent()) {
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

        userRepository.save(user);
        return enable2FA
                ? "2FA enabled. Please scan the QR code with your authenticator app."
                : "Registered successfully (2FA disabled)";
    }

    // --- Login ---
    public String login(String username, String password, Integer otp) {
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

        return jwtTokenUtil.generateToken(username);
    }
}