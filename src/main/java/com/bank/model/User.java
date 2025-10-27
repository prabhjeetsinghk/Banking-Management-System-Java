package com.bank.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a user (customer or admin) in the banking system.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password; // Stored as BCrypt hash

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private String role = "ROLE_USER"; // Default role

    @Column
    private String twoFactorSecret; // Optional TOTP secret for 2FA

    @Column
    private boolean twoFactorEnabled = false;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters & Setters
    // ... (for brevity, include all standard getters/setters)
}