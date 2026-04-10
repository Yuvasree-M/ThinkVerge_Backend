package com.thinkverge.lms.service;

import com.thinkverge.lms.model.User;
import com.thinkverge.lms.enums.Role;
import com.thinkverge.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow();
    }

    public List<User> getAllInstructors() {
        return userRepository.findByRole(Role.INSTRUCTOR);
    }

    public List<User> getAllStudents() {
        return userRepository.findByRole(Role.STUDENT);
    }

    public void updateLastSeen(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow();

        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);
    }

    public User myProfile(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow();
    }
    
 // admin - all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // admin - change role
    public User changeRole(Long id, Role role) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(role);
        return userRepository.save(user);
    }

    // admin - delete
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}