package com.bank.controller;

import com.bank.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        String email = (String) payload.get("email");
        String password = (String) payload.get("password");
        boolean enable2FA = payload.get("enable2FA") != null && (boolean) payload.get("enable2FA");

        String result = authService.register(username, email, password, enable2FA);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        String password = (String) payload.get("password");
        Integer otp = payload.get("otp") != null ? (Integer) payload.get("otp") : null;

        String token = authService.login(username, password, otp);
        return ResponseEntity.ok(Map.of("token", token));
    }
}