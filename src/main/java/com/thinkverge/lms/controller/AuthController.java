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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final TokenBlocklist tokenBlocklist;

    @Value("${app.prod:false}")
    private boolean isProd;

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        try {
            AuthResponse auth = authService.register(request);
            // No cookie set — user must await admin approval before they can log in
            return ResponseEntity.ok(auth);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        try {
            AuthResponse auth = authService.login(request);

            if (auth.getToken() != null) {
                // Use raw Set-Cookie header to support SameSite=None (Java Cookie API doesn't support it)
                // SameSite=None + Secure are REQUIRED for cross-site cookies (Netlify → Render)
                String cookieHeader = String.format(
                    "jwt=%s; HttpOnly; Path=/; Max-Age=%d; SameSite=%s%s",
                    auth.getToken(),
                    7 * 24 * 60 * 60,          // 7 days
                    isProd ? "None" : "Lax",    // None required for cross-site in prod
                    isProd ? "; Secure" : ""    // Secure required when SameSite=None
                );
                response.setHeader("Set-Cookie", cookieHeader);
            }

            return ResponseEntity.ok(auth);

        } catch (RuntimeException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Invalid credentials";
            if (msg.toLowerCase().contains("pending") || msg.toLowerCase().contains("approval")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(java.util.Map.of("message", msg));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("message", "Invalid email or password"));
        }
    }

    // ================= LOGOUT =================
    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        String token = extractTokenFromCookie(request);

        if (token != null) {
            try {
                String jti = jwtService.extractJti(token);
                long expiry = jwtService.getTokenExpiry(token);
                tokenBlocklist.revokeToken(jti, expiry);
            } catch (Exception ignored) {}
        }

        // Clear cookie — must mirror the same attributes used when setting it
        String cookieHeader = String.format(
            "jwt=; HttpOnly; Path=/; Max-Age=0; SameSite=%s%s",
            isProd ? "None" : "Lax",
            isProd ? "; Secure" : ""
        );
        response.setHeader("Set-Cookie", cookieHeader);
    }

    // ================= EXTRACT TOKEN =================
    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("jwt".equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}