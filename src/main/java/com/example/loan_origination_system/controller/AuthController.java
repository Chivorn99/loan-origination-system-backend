package com.example.loan_origination_system.controller;

import com.example.loan_origination_system.dto.AuthRequest;
import com.example.loan_origination_system.dto.AuthResponse;
import com.example.loan_origination_system.dto.RegisterRequest;
import com.example.loan_origination_system.security.JwtUtil;
import com.example.loan_origination_system.security.TokenBlacklist;
import com.example.loan_origination_system.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenBlacklist tokenBlacklist;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            // 1. Spring Security checks the username and password here
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
            );
        } catch (Exception e) {
            // If it fails, return a 401 Unauthorized
            return ResponseEntity.status(401).body("Error: Incorrect username or password!");
        }

        // 2. If password is correct, fetch the user's details
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.username());

        // 3. Generate the JWT Token
        final String jwt = jwtUtil.generateToken(userDetails);

        // 4. Return the token in JSON format
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Blacklist the token so it can never be used again
            tokenBlacklist.blacklist(token, jwtUtil.extractExpiration(token));
        }
        // Clear the security context for this request
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logged out successfully.");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Get current authenticated user's username from security context
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            
            // Register new user (only superadmin can do this)
            var newUser = userService.registerUser(registerRequest, currentUsername);
            
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}