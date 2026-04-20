package com.thinkverge.lms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlocklist tokenBlocklist;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // ✅ Skip auth endpoints
        if (path.startsWith("/api/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (token != null) {
            try {

                // ✅ BLOCKLIST CHECK
                String jti = jwtService.extractJti(token);
                if (tokenBlocklist.isRevoked(jti)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                String username = jwtService.extractUsername(token);
                String role = jwtService.extractUserRole(token);

                if (username != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {

                    UserDetails userDetails;

                    try {
                        userDetails = userDetailsService.loadUserByUsername(username);
                    } catch (UsernameNotFoundException ex) {
                        chain.doFilter(request, response);
                        return;
                    }

                    if (jwtService.validateToken(token)) {

                        List<SimpleGrantedAuthority> authorities =
                                Collections.singletonList(
                                        new SimpleGrantedAuthority("ROLE_" + role)
                                );

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        authorities
                                );

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }

            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }

    // ✅ READ TOKEN FROM COOKIE (IMPORTANT CHANGE)
    private String extractToken(HttpServletRequest request) {

        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("jwt".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}