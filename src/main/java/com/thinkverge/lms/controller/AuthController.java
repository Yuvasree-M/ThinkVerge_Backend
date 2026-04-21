package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.request.LoginRequest;
import com.thinkverge.lms.dto.request.RegisterRequest;
import com.thinkverge.lms.dto.response.AuthResponse;
import com.thinkverge.lms.security.JwtService;
import com.thinkverge.lms.security.TokenBlocklist;
import com.thinkverge.lms.service.AuthService;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
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

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse auth = authService.register(request);
            // No token returned — user must await admin approval before logging in
            return ResponseEntity.ok(auth);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse auth = authService.login(request);
            // ✅ Token returned in response body — frontend saves to localStorage
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
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Revoke token via Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String jti = jwtService.extractJti(token);
                long expiry = jwtService.getTokenExpiry(token);
                tokenBlocklist.revokeToken(jti, expiry);
            } catch (Exception ignored) {}
        }
        // Frontend removes token from localStorage
        return ResponseEntity.ok().build();
    }
}