package com.example.loan_origination_system.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // We will create this class in the next step!
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // 1. Check if the request has a "Bearer " token in the header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // Remove "Bearer " to get the pure token
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                System.out.println("Invalid or expired JWT Token");
            }
        }

        // 2. If we found a username, and they aren't authenticated yet...
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Fetch the user from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 3. Validate the token using our JwtUtil
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // 4. Token is good! Tell Spring Security to log them in for this request
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Let the request continue to the Controller
        filterChain.doFilter(request, response);
    }
}