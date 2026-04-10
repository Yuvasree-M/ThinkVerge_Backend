package com.thinkverge.lms.security;

import com.thinkverge.lms.model.User;
import com.thinkverge.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + email)
                );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(
                        Collections.singletonList(
                                new SimpleGrantedAuthority(
                                        user.getRole().name()
                                )
                        )
                )
                .build();
    }
}