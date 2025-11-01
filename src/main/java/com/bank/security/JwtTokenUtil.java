package com.bank.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT generation and validation utility.
 */
@Component
public class JwtTokenUtil {

    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour
    private static final String ISSUER = "BankingSystem";

    // Production: store securely (Vault, environment variable)
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // public String generateToken(String username, String accountNumber) {
    //     return Jwts.builder()
    //             .setSubject(username)
    //             .setIssuer(ISSUER)
    //             .setIssuedAt(new Date())
    //             .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
    //             .signWith(key)
    //             .compact();
    // }

    public String generateToken(String username, String accountNumber) {
        return Jwts.builder()        // include the claims map
                .setSubject(username)       // keep username as subject
                .setIssuer(ISSUER)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .claim("accountNumber", accountNumber)
                .signWith(key)
                .compact();
        }

    public String getClaim(String token, String claimKey) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get(claimKey, String.class);
    }


    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
