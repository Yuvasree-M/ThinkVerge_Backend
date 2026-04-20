package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.request.LoginRequest;
import com.thinkverge.lms.dto.request.RegisterRequest;
import com.thinkverge.lms.dto.response.AuthResponse;
import com.thinkverge.lms.security.JwtService;
import com.thinkverge.lms.security.TokenBlocklist;
import com.thinkverge.lms.service.AuthService;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final TokenBlocklist tokenBlocklist;

    // ✅ AUTO SWITCH (set true in Render)
    @Value("${app.prod:false}")
    private boolean isProd;

    // ================= REGISTER =================
    @PostMapping("/register")
    public AuthResponse register(
            @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthResponse auth = authService.register(request);

        return auth;
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse auth = authService.login(request);

        return auth;
    }

    // ================= LOGOUT =================
    @PostMapping("/logout")
    public void logout(HttpServletRequest request,
                       HttpServletResponse response) {

        String token = extractTokenFromCookie(request);

        if (token != null) {
            String jti = jwtService.extractJti(token);
            long expiry = jwtService.getTokenExpiry(token);
            tokenBlocklist.revokeToken(jti, expiry);
        }

        // ❌ Delete cookie
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(isProd);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }



    // ================= EXTRACT TOKEN =================
    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("jwt".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}