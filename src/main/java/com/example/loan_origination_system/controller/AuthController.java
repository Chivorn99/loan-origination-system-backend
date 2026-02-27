package com.example.loan_origination_system.controller;

import com.example.loan_origination_system.dto.AuthRequest;
import com.example.loan_origination_system.dto.AuthResponse;
import com.example.loan_origination_system.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
}