//package com.thinkverge.lms.service;
//
//import com.thinkverge.lms.model.User;
//import com.thinkverge.lms.enums.Role;
//import com.thinkverge.lms.repository.UserRepository;
//
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class UserService {
//
//    private final UserRepository userRepository;
//
//    public User getByEmail(String email) {
//        return userRepository
//                .findByEmail(email)
//                .orElseThrow();
//    }
//
//    public List<User> getAllInstructors() {
//        return userRepository.findByRole(Role.INSTRUCTOR);
//    }
//
//    public List<User> getAllStudents() {
//        return userRepository.findByRole(Role.STUDENT);
//    }
//
//    public void updateLastSeen(String email) {
//        User user = userRepository
//                .findByEmail(email)
//                .orElseThrow();
//
//        user.setLastSeen(LocalDateTime.now());
//        userRepository.save(user);
//    }
//
//    public User myProfile(String email) {
//        return userRepository
//                .findByEmail(email)
//                .orElseThrow();
//    }
//    
// // admin - all users
//    public List<User> getAllUsers() {
//        return userRepository.findAll();
//    }
//
//    // admin - change role
//    public User changeRole(Long id, Role role) {
//        User user = userRepository.findById(id).orElseThrow();
//        user.setRole(role);
//        return userRepository.save(user);
//    }
//
//    // admin - delete
//    public void deleteUser(Long id) {
//        userRepository.deleteById(id);
//    }
//}

package com.thinkverge.lms.service;

import com.thinkverge.lms.model.User;
import com.thinkverge.lms.enums.Role;
import com.thinkverge.lms.repository.UserRepository;
import com.thinkverge.lms.service.EmailService;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final Cloudinary cloudinary; // ✅ ADD THIS
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    
    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    // ── Only return approved instructors/students for use in app ──
    public List<User> getAllInstructors() {
        return userRepository.findByRoleAndApprovedTrue(Role.INSTRUCTOR);
    }

    public List<User> getAllStudents() {
        return userRepository.findByRoleAndApprovedTrue(Role.STUDENT);
    }

    public void updateLastSeen(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);
    }

    public User myProfile(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    // ── Admin: all users ──────────────────────────────────────
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ── Admin: users pending approval ─────────────────────────
    public List<User> getPendingUsers() {
        return userRepository.findByApprovedFalse();
    }

    // ── Admin: approve a user ─────────────────────────────────
    public User approveUser(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setApproved(true);
        userRepository.save(user);

        // ✅ Notify the user that their account is now active
        try {
            String body = """
                    <div style="font-family:Arial,sans-serif;padding:24px;background:#f9f9f9">
                      <div style="max-width:600px;margin:0 auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08)">
                        <div style="background:linear-gradient(135deg,#1e3a6f,#2d5be3);padding:24px;text-align:center">
                          <h1 style="color:white;margin:0;font-size:20px">Account Approved! 🎉</h1>
                          <p style="color:rgba(255,255,255,0.7);margin:8px 0 0;font-size:13px">ThinkVerge LMS</p>
                        </div>
                        <div style="padding:28px">
                          <p style="color:#334155;font-size:15px;margin:0 0 16px">Hi %s,</p>
                          <p style="color:#475569;font-size:14px;line-height:1.7;margin:0 0 24px">
                            Great news! Your ThinkVerge account has been approved by an administrator. You can now log in and start using the platform.
                          </p>
                          <div style="text-align:center;margin:24px 0">
                            <a href="#" style="display:inline-block;background:linear-gradient(135deg,#1e3a6f,#2d5be3);color:white;text-decoration:none;padding:12px 32px;border-radius:8px;font-weight:700;font-size:14px">
                              Log In to ThinkVerge →
                            </a>
                          </div>
                          <p style="color:#64748b;font-size:13px;margin:0">Welcome to the ThinkVerge learning community!</p>
                        </div>
                        <div style="background:#f8fafc;padding:16px 28px;border-top:1px solid #e2e8f0">
                          <p style="margin:0;color:#94a3b8;font-size:12px">ThinkVerge LMS — Automated Notification</p>
                        </div>
                      </div>
                    </div>
                    """.formatted(user.getName());
            emailService.send(user.getEmail(), "Your ThinkVerge Account is Approved!", body);
        } catch (Exception e) {
            System.err.println("[UserService] Failed to send approval email: " + e.getMessage());
        }

        return user;
    }

    // ── Admin: change role ────────────────────────────────────
    public User changeRole(Long id, Role role) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(role);
        return userRepository.save(user);
    }

    // ── Admin: delete user ────────────────────────────────────
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    


    public User updateProfileImage(String email, MultipartFile file) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), Map.of(
                    "folder", "profile-images",
                    "resource_type", "image"
            ));

            String imageUrl = result.get("secure_url").toString();

            User user = userRepository.findByEmail(email).orElseThrow();
            user.setProfileImage(imageUrl);

            return userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Profile image upload failed: " + e.getMessage());
        }
    }
    
    public User updateProfile(String email, String name, String currentPassword, String newPassword, MultipartFile file) {
        User user = userRepository.findByEmail(email).orElseThrow();

        if (name != null && !name.isBlank()) {
            user.setName(name);
        }

        if (newPassword != null && !newPassword.isBlank()) {
            // ✅ Must provide current password to change it
            if (currentPassword == null || currentPassword.isBlank()) {
                throw new RuntimeException("Current password is required");
            }
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        if (file != null && !file.isEmpty()) {
            try {
                Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), Map.of(
                    "folder", "profile-images",
                    "resource_type", "image"
                ));
                user.setProfileImage(result.get("secure_url").toString());
            } catch (Exception e) {
                throw new RuntimeException("Profile image upload failed: " + e.getMessage());
            }
        }

        return userRepository.save(user);
    }
    public User deleteProfileImage(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setProfileImage(null);
        return userRepository.save(user);
    }
}
