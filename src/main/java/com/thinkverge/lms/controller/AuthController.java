package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.request.LoginRequest;
import com.thinkverge.lms.dto.request.RegisterRequest;
import com.thinkverge.lms.dto.response.AuthResponse;
import com.thinkverge.lms.security.JwtService;
import com.thinkverge.lms.security.TokenBlocklist;
import com.thinkverge.lms.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final TokenBlocklist tokenBlocklist;

    @PostMapping("/register")
    public AuthResponse register(
            @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer "))
            return;

        String token = header.substring(7);

        String jti = jwtService.extractJti(token);

        long expiry = jwtService.getTokenExpiry(token);

        tokenBlocklist.revokeToken(jti, expiry);
    }
}