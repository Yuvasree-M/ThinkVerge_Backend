//package com.thinkverge.lms.controller;
//
//import com.thinkverge.lms.dto.request.LoginRequest;
//import com.thinkverge.lms.dto.request.RegisterRequest;
//import com.thinkverge.lms.dto.response.AuthResponse;
//import com.thinkverge.lms.security.JwtService;
//import com.thinkverge.lms.security.TokenBlocklist;
//import com.thinkverge.lms.service.AuthService;
//
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//
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
//    @PostMapping("/register")
//    public AuthResponse register(
//            @RequestBody RegisterRequest request
//    ) {
//        return authService.register(request);
//    }
//
//    @PostMapping("/login")
//    public AuthResponse login(
//            @RequestBody LoginRequest request,
//            HttpServletResponse response
//    ) {
//        AuthResponse auth = authService.login(request);
//
//        Cookie cookie = new Cookie("jwt", auth.getToken());
//        cookie.setHttpOnly(true);
//        cookie.setSecure(true); // ✅ HTTPS only
//        cookie.setPath("/");
//
//        response.addCookie(cookie);
//
//        // ✅ Add SameSite=None manually
//        response.addHeader("Set-Cookie",
//            "jwt=" + auth.getToken() + "; Path=/; HttpOnly; Secure; SameSite=None");
//
//        return auth;
//    }
//
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
//        // ✅ DELETE COOKIE
//        Cookie cookie = new Cookie("jwt", null);
//        cookie.setHttpOnly(true);
//        cookie.setPath("/");
//        cookie.setMaxAge(0);
//
//        response.addCookie(cookie);
//    }
//    
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final TokenBlocklist tokenBlocklist;

    // ✅ REGISTER
//    @PostMapping("/register")
//    public AuthResponse register(@RequestBody RegisterRequest request,
//                                 HttpServletResponse response) {
//
//        AuthResponse auth = authService.register(request);
//        setCookie(response, auth.getToken());
//
//        return auth;
//    }
    @PostMapping("/register")
    public AuthResponse register(
            @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthResponse auth = authService.register(request);

        Cookie cookie = new Cookie("jwt", auth.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");

        response.addCookie(cookie);

        response.addHeader("Set-Cookie",
            "jwt=" + auth.getToken() + "; Path=/; HttpOnly; Secure; SameSite=None");

        return auth;
    }
    // ✅ LOGIN
//    @PostMapping("/login")
//    public AuthResponse login(@RequestBody LoginRequest request,
//                              HttpServletResponse response) {
//
//        AuthResponse auth = authService.login(request);
//        setCookie(response, auth.getToken());
//
//        return auth;
//    }
    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse auth = authService.login(request);

        // ✅ CREATE COOKIE
        Cookie cookie = new Cookie("jwt", auth.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // ✅ HERE
        cookie.setPath("/");

        response.addCookie(cookie);

        // ✅ ADD SameSite=None HERE
        response.addHeader("Set-Cookie",
            "jwt=" + auth.getToken() + "; Path=/; HttpOnly; Secure; SameSite=None");

        return auth;
    }
    // ✅ LOGOUT (BLOCKLIST + DELETE COOKIE)
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
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }

    // 🔧 Helper: Set Cookie
    private void setCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 🔴 true in production
        cookie.setPath("/");
        // ❌ no setMaxAge → session cookie

        response.addCookie(cookie);
    }

    // 🔧 Helper: Extract Cookie
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