package com.thinkverge.lms.controller;

import com.thinkverge.lms.model.User;
import com.thinkverge.lms.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    // all instructors
    @GetMapping("/instructors")
    public List<User> instructors() {
        return userService.getAllInstructors();
    }

    // all students
    @GetMapping("/students")
    public List<User> students() {
        return userService.getAllStudents();
    }

    // update last seen
    @PutMapping("/last-seen")
    public void lastSeen(Authentication auth) {
        userService.updateLastSeen(auth.getName());
    }
}