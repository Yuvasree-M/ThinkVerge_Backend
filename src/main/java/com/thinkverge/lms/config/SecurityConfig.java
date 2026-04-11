package com.thinkverge.lms.config;

import com.thinkverge.lms.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

            	    // Public endpoints
            	    .requestMatchers("/api/auth/**").permitAll()
            	    .requestMatchers("/api/courses").permitAll()
            	    .requestMatchers("/api/courses/*").permitAll()

            	    // Admin endpoints
            	    .requestMatchers("/api/courses/admin/**").hasRole("ADMIN")
            	    .requestMatchers("/api/admin/**").hasRole("ADMIN")

            	 // Instructor endpoints
            	    .requestMatchers("/api/courses/instructor/**").hasRole("INSTRUCTOR")
            	    .requestMatchers("/api/instructor/**").hasRole("INSTRUCTOR")
            	    .requestMatchers("/api/modules/**").hasRole("INSTRUCTOR")   
            	    .requestMatchers("/api/lessons/**").hasRole("INSTRUCTOR")   

            	    // Student endpoints
            	    .requestMatchers("/api/student/**").hasRole("STUDENT")

            	    // Any other request requires auth
            	    .anyRequest().authenticated()
            	)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",   // Vite React
                "http://127.0.0.1:5173",
                "https://thinkverge.netlify.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}