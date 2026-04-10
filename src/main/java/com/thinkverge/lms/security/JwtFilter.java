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

        String token = extractToken(request);

        if (token != null) {

            try {

                // check revoked
                String jti = jwtService.extractJti(token);
                if (tokenBlocklist.isRevoked(jti)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                // extract email
                String username = jwtService.extractUsername(token);
                String role = jwtService.extractUserRole(token);

                // if already authenticated skip
                if (username != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {

                    // load user from DB (IMPORTANT)
                    UserDetails userDetails;
                    try {
                        userDetails = userDetailsService.loadUserByUsername(username);
                    } catch (UsernameNotFoundException ex) {
                        // email changed in DB -> reject token
                        chain.doFilter(request, response);
                        return;
                    }

                    // validate token
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

                        SecurityContextHolder.getContext()
                                .setAuthentication(auth);
                    }
                }

            } catch (Exception e) {
                // invalid token ignore
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer "))
            return null;

        return header.substring(7);
    }
}