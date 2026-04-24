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

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // current user
    @GetMapping("/me")
    public User me(Authentication auth) {
        return userService.myProfile(auth.getName());
    }

    // all approved instructors
    @GetMapping("/instructors")
    public List<User> instructors() {
        return userService.getAllInstructors();
    }

    // all approved students
    @GetMapping("/students")
    public List<User> students() {
        return userService.getAllStudents();
    }

    // ─── ADMIN ──────────────────────────────────────────────────

    // all users (admin)
    @GetMapping
    public List<User> allUsers() {
        return userService.getAllUsers();
    }

    // pending approval users (admin)
    @GetMapping("/pending")
    public List<User> pendingUsers() {
        return userService.getPendingUsers();
    }

    // approve a user (admin)
    @PutMapping("/{id}/approve")
    public User approveUser(@PathVariable Long id) {
        return userService.approveUser(id);
    }

    // change role (admin)
    @PutMapping("/{id}/role")
    public User changeRole(
            @PathVariable Long id,
            @RequestParam Role role
    ) {
        return userService.changeRole(id, role);
    }

    // delete user (admin)
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    // update last seen
    @PutMapping("/last-seen")
    public void updateLastSeen(Authentication auth) {
        userService.updateLastSeen(auth.getName());
    }
    @PostMapping("/api/users/profile-image")
    public User updateProfileImage(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {

        String email = auth.getName();
        return userService.updateProfileImage(email, file);
    }
}
