package com.example.loan_origination_system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // This is the "Master Password" used to sign the tokens.
    // In production, you would put this in your application.properties!
    @Value("${jwt.secret:ThisIsASecretKeyForJwtAuthenticationInPawnShopManagementSystem2026!}")
    private String secret;

    // Token is valid for 24 Hours (in milliseconds)
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    // Creates the cryptographic key
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 1. EXTRACT: Get the username out of the token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 2. GENERATE: Create a new token for a user
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // You can embed extra data in the token here if you want (like roles)
        claims.put("roles", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // Usually the username
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 3. VALIDATE: Check if the token belongs to the user and is still active
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}