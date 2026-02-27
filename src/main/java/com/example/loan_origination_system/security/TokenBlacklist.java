package com.example.loan_origination_system.security;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory blacklist for invalidated JWT tokens.
 * Tokens are stored until their natural expiration date, then auto-cleaned.
 */
@Component
public class TokenBlacklist {

    // Map of token -> expiration time
    private final Map<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklist(String token, Date expiration) {
        blacklistedTokens.put(token, expiration);
        cleanExpired(); // Clean up old entries while we're here
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    // Remove tokens that have already expired (they are useless to keep)
    private void cleanExpired() {
        Date now = new Date();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
    }
}

