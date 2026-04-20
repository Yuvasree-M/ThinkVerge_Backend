//package com.thinkverge.lms.service;
//
//import com.thinkverge.lms.dto.request.LoginRequest;
//import com.thinkverge.lms.dto.request.RegisterRequest;
//import com.thinkverge.lms.dto.response.AuthResponse;
//import com.thinkverge.lms.model.User;
//import com.thinkverge.lms.repository.UserRepository;
//import com.thinkverge.lms.security.JwtService;
//
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtService jwtService;
//
//    public AuthResponse register(RegisterRequest request) {
//
//        User user = User.builder()
//                .name(request.getName())
//                .email(request.getEmail())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .role(request.getRole())
//                .build();
//
//        userRepository.save(user);
//
//        String token = jwtService.generateToken(
//                user.getEmail(),
//                user.getRole().name()
//        );
//
//        return AuthResponse.builder()
//                .token(token)
//                .email(user.getEmail())
//                .name(user.getName())
//                .role(user.getRole())
//                .build();
//    }
//
//    public AuthResponse login(LoginRequest request) {
//
//        User user = userRepository
//                .findByEmail(request.getEmail())
//                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
//
//        if (!passwordEncoder.matches(
//                request.getPassword(),
//                user.getPassword()
//        )) {
//            throw new RuntimeException("Invalid credentials");
//        }
//
//        String token = jwtService.generateToken(
//                user.getEmail(),
//                user.getRole().name()
//        );
//
//        return AuthResponse.builder()
//                .token(token)
//                .email(user.getEmail())
//                .name(user.getName())
//                .role(user.getRole())
//                .build();
//    }
//}

package com.thinkverge.lms.service;

import com.thinkverge.lms.dto.request.LoginRequest;
import com.thinkverge.lms.dto.request.RegisterRequest;
import com.thinkverge.lms.dto.response.AuthResponse;
import com.thinkverge.lms.enums.Role;
import com.thinkverge.lms.model.User;
import com.thinkverge.lms.repository.UserRepository;
import com.thinkverge.lms.security.JwtService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${app.admin-email:admin@thinkverge.com}")
    private String adminEmail;

    // ──────────────────────────────────────────────────────────
    // REGISTER
    // ──────────────────────────────────────────────────────────
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // ADMIN accounts are auto-approved; STUDENT/INSTRUCTOR require admin review
        boolean autoApproved = request.getRole() == Role.ADMIN;

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .approved(autoApproved)
                .build();

        userRepository.save(user);

        // Notify admin about the new registration (fire-and-forget)
        if (!autoApproved) {
            notifyAdminNewRegistration(user);
        }

        // Return without a token — user must wait for admin approval before login
        return AuthResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .approved(autoApproved)
                .build();
    }

    // ──────────────────────────────────────────────────────────
    // LOGIN
    // ──────────────────────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // ✅ Block unapproved users
        if (Boolean.FALSE.equals(user.getApproved())) {
            throw new RuntimeException("Your account is pending admin approval. Please wait for an administrator to approve your registration.");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .approved(true)
                .build();
    }

    // ──────────────────────────────────────────────────────────
    // Notify admin of new registration
    // ──────────────────────────────────────────────────────────
    private void notifyAdminNewRegistration(User user) {
        try {
            // Find all admin users to notify them
            List<User> admins = userRepository.findByRole(Role.ADMIN);

            String body = """
                    <div style="font-family:Arial,sans-serif;padding:24px;background:#f9f9f9">
                      <div style="max-width:600px;margin:0 auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08)">
                        <div style="background:linear-gradient(135deg,#1e3a6f,#2d5be3);padding:24px;text-align:center">
                          <h1 style="color:white;margin:0;font-size:20px">New User Registration</h1>
                          <p style="color:rgba(255,255,255,0.7);margin:8px 0 0;font-size:13px">ThinkVerge LMS — Admin Action Required</p>
                        </div>
                        <div style="padding:28px">
                          <p style="color:#334155;font-size:15px;margin:0 0 20px">
                            A new user has registered and is awaiting your approval:
                          </p>
                          <table style="width:100%%;border-collapse:collapse">
                            <tr><td style="padding:8px 0;color:#64748b;font-size:13px;width:120px">Name</td><td style="padding:8px 0;color:#1e293b;font-weight:600;font-size:14px">%s</td></tr>
                            <tr><td style="padding:8px 0;color:#64748b;font-size:13px">Email</td><td style="padding:8px 0;color:#1e293b;font-size:14px">%s</td></tr>
                            <tr><td style="padding:8px 0;color:#64748b;font-size:13px">Role</td><td style="padding:8px 0"><span style="background:#dbeafe;color:#1d4ed8;padding:3px 10px;border-radius:20px;font-size:12px;font-weight:700">%s</span></td></tr>
                          </table>
                          <div style="margin:24px 0;padding:16px;background:#fef9ec;border-left:4px solid #f59e0b;border-radius:4px">
                            <p style="margin:0;color:#92400e;font-size:13px">
                              ⚠️ This user cannot log in until you approve their account from the Admin → Users → Pending Approval tab.
                            </p>
                          </div>
                          <p style="color:#64748b;font-size:13px;margin:0">Log in to ThinkVerge Admin to approve or reject this registration.</p>
                        </div>
                        <div style="background:#f8fafc;padding:16px 28px;border-top:1px solid #e2e8f0">
                          <p style="margin:0;color:#94a3b8;font-size:12px">ThinkVerge LMS — Automated Notification</p>
                        </div>
                      </div>
                    </div>
                    """.formatted(user.getName(), user.getEmail(), user.getRole().name());

            // Notify all existing admins
            if (!admins.isEmpty()) {
                for (User admin : admins) {
                    emailService.send(admin.getEmail(),
                            "New Registration Pending Approval — " + user.getName(),
                            body);
                }
            } else {
                // Fallback to configured admin email if no admin user exists yet
                emailService.send(adminEmail,
                        "New Registration Pending Approval — " + user.getName(),
                        body);
            }
        } catch (Exception e) {
            // Never let email failure break registration
            System.err.println("[AuthService] Failed to send admin notification: " + e.getMessage());
        }
    }
}
