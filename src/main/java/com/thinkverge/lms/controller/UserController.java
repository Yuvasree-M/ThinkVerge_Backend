//package com.thinkverge.lms.controller;
//
//import com.thinkverge.lms.enums.Role;
//import com.thinkverge.lms.model.User;
//import com.thinkverge.lms.service.UserService;
//
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/users")
//@RequiredArgsConstructor
//public class UserController {
//
//    private final UserService userService;
//
//    // current user
//    @GetMapping("/me")
//    public User me(Authentication auth) {
//        return userService.myProfile(auth.getName());
//    }
//
//    // all instructors
//    @GetMapping("/instructors")
//    public List<User> instructors() {
//        return userService.getAllInstructors();
//    }
//
//    // all students
//    @GetMapping("/students")
//    public List<User> students() {
//        return userService.getAllStudents();
//    }
//
//    // ================= ADMIN =================
//
//    // all users (admin)
//    @GetMapping
//    public List<User> allUsers() {
//        return userService.getAllUsers();
//    }
//
//    // change role
//    @PutMapping("/{id}/role")
//    public User changeRole(
//            @PathVariable Long id,
//            @RequestParam Role role
//    ) {
//        return userService.changeRole(id, role);
//    }
//
//    // delete user
//    @DeleteMapping("/{id}")
//    public void deleteUser(@PathVariable Long id) {
//        userService.deleteUser(id);
//    }
//
//    // update last seen
//    @PutMapping("/last-seen")
//    public void lastSeen(Authentication auth) {
//        userService.updateLastSeen(auth.getName());
//    }
//}


package com.thinkverge.lms.controller;

import com.thinkverge.lms.enums.Role;
import com.thinkverge.lms.model.User;
import com.thinkverge.lms.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public User me(Authentication auth) {
        return userService.myProfile(auth.getName());
    }

    @GetMapping("/instructors")
    public List<User> instructors() {
        return userService.getAllInstructors();
    }

    @GetMapping("/students")
    public List<User> students() {
        return userService.getAllStudents();
    }

    @GetMapping
    public List<User> allUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/pending")
    public List<User> pendingUsers() {
        return userService.getPendingUsers();
    }

    @PutMapping("/{id}/approve")
    public User approveUser(@PathVariable Long id) {
        return userService.approveUser(id);
    }

    @PutMapping("/{id}/role")
    public User changeRole(@PathVariable Long id, @RequestParam Role role) {
        return userService.changeRole(id, role);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PutMapping("/last-seen")
    public void updateLastSeen(Authentication auth) {
        userService.updateLastSeen(auth.getName());
    }

    // ── POST: first-time profile image upload ──────────────────
    @PostMapping("/profile-image")
    public User uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        return userService.updateProfileImage(auth.getName(), file);
    }
    @DeleteMapping("/profile-image")
    public User deleteProfileImage(Authentication auth) {
        return userService.deleteProfileImage(auth.getName());
    }
    // ── PUT: edit profile (name, password, or replace image) ──
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestParam(value = "file",            required = false) MultipartFile file,
            @RequestParam(value = "name",            required = false) String name,
            @RequestParam(value = "currentPassword", required = false) String currentPassword,
            @RequestParam(value = "newPassword",     required = false) String newPassword,
            Authentication auth) {
        try {
            User updated = userService.updateProfile(auth.getName(), name, currentPassword, newPassword, file);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            // Returns 400 with the error message so frontend can show it
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}