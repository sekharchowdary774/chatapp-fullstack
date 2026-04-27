package com.example.chatapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // DEBUG
        System.out.println("🔍 Request: " + method + " " + path);

        // 🚨 SKIP JWT FOR ALL PUBLIC ENDPOINTS (MATCH SecurityConfig)
        if (method.equalsIgnoreCase("OPTIONS") ||
                path.startsWith("/api/auth/") ||     // login / register
                path.startsWith("/api/users/") ||    // FIXED: skip ALL user routes (search included)
                path.startsWith("/api/file/") ||
                path.startsWith("/api/chat/") ||
                path.startsWith("/chat/") ||
                path.startsWith("/ws/") ||
                path.startsWith("/topic/") ||
                path.startsWith("/app/")) {

            System.out.println("✅ Skipping JWT for: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        // ----- JWT AUTH FOR PROTECTED ROUTES -----
        final String authHeader = request.getHeader("Authorization");
        String email = null;
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                email = jwtUtil.extractEmail(token);
            } catch (Exception e) {
                System.out.println("❌ Invalid JWT: " + e.getMessage());
            }
        }

        // Validate token and set auth in context
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtUtil.validateToken(token, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
