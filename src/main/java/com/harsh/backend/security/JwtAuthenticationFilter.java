package com.harsh.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        if (path.equals("/") || path.equals("/error")) return true;
        if (path.equals("/api/users/login") && method.equals("POST")) return true;
        if (path.equals("/api/users") && method.equals("POST")) return true;
        if (path.equals("/api/code/run") && method.equals("POST")) return true;
        if (path.equals("/api/ai/generate") && method.equals("POST")) return true;
        if (path.equals("/api/ai/generate/coding") && method.equals("POST")) return true;
        if (path.equals("/api/ai/review") && method.equals("POST")) return true;
        if (path.startsWith("/api/questions") && method.equals("GET")) return true;
        if (path.equals("/api/interview/leaderboard") && method.equals("GET")) return true;
        if (path.startsWith("/api/ai/mock-interview")) return true;
        if (path.equals("/api/feedback") && method.equals("POST")) return true;
        if (path.equals("/api/ai/chat") && method.equals("POST")) return true;
        if (path.startsWith("/api/ai/resume")) return true;
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        try {
            String email = jwtUtil.extractEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtUtil.validateToken(token)) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    null,
                                    Collections.emptyList()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // IMPORTANT: don't crash; just continue unauthenticated
            // You can temporarily log it:
            System.out.println("JWT Filter error: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}