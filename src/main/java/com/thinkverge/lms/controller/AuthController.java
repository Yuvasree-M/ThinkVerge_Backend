//package com.thinkverge.lms.controller;
//
//import com.thinkverge.lms.dto.request.LoginRequest;
//import com.thinkverge.lms.dto.request.RegisterRequest;
//import com.thinkverge.lms.dto.response.AuthResponse;
//import com.thinkverge.lms.security.JwtService;
//import com.thinkverge.lms.security.TokenBlocklist;
//import com.thinkverge.lms.service.AuthService;
//
//import jakarta.servlet.http.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//public class AuthController {
//
//    private final AuthService authService;
//    private final JwtService jwtService;
//    private final TokenBlocklist tokenBlocklist;
//
//    // ✅ AUTO SWITCH (set true in Render)
//    @Value("${app.prod:false}")
//    private boolean isProd;
//
//    // ================= REGISTER =================
//    @PostMapping("/register")
//    public AuthResponse register(
//            @RequestBody RegisterRequest request,
//            HttpServletResponse response
//    ) {
//        AuthResponse auth = authService.register(request);
//
//        return auth;
//    }
//
//    // ================= LOGIN =================
//    @PostMapping("/login")
//    public AuthResponse login(
//            @RequestBody LoginRequest request,
//            HttpServletResponse response
//    ) {
//        AuthResponse auth = authService.login(request);
//
//        return auth;
//    }
//
//    // ================= LOGOUT =================
//    @PostMapping("/logout")
//    public void logout(HttpServletRequest request,
//                       HttpServletResponse response) {
//
//        String token = extractTokenFromCookie(request);
//
//        if (token != null) {
//            String jti = jwtService.extractJti(token);
//            long expiry = jwtService.getTokenExpiry(token);
//            tokenBlocklist.revokeToken(jti, expiry);
//        }
//
//        // ❌ Delete cookie
//        Cookie cookie = new Cookie("jwt", null);
//        cookie.setHttpOnly(true);
//        cookie.setSecure(isProd);
//        cookie.setPath("/");
//        cookie.setMaxAge(0);
//
//        response.addCookie(cookie);
//    }
//
//
//
//    // ================= EXTRACT TOKEN =================
//    private String extractTokenFromCookie(HttpServletRequest request) {
//        if (request.getCookies() == null) return null;
//
//        for (Cookie cookie : request.getCookies()) {
//            if ("jwt".equals(cookie.getName())) {
//                return cookie.getValue();
//            }
//        }
//        return null;
//    }
//}
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

            // ✅ Set HttpOnly cookie only when login succeeds and token exists
            if (auth.getToken() != null) {
                Cookie cookie = new Cookie("jwt", auth.getToken());
                cookie.setHttpOnly(true);
                cookie.setSecure(isProd);
                cookie.setPath("/");
                cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
                response.addCookie(cookie);
            }

            return ResponseEntity.ok(auth);

        } catch (RuntimeException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Invalid credentials";
            // Pending approval → 403, invalid credentials → 401
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

        // Clear the JWT cookie
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
            if ("jwt".equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}
