package com.thinkverge.lms.config;

import com.thinkverge.lms.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import org.springframework.web.multipart.support.MultipartFilter;

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

            	    // Public
            	    .requestMatchers("/api/auth/**").permitAll()
            	    .requestMatchers("/api/courses").permitAll()
            	    .requestMatchers("/api/courses/*").permitAll()

            	    // ✅ FIRST: allow GET for all roles
            	    .requestMatchers(HttpMethod.GET, "/api/courses/**")
            	        .hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")

            	    .requestMatchers(HttpMethod.GET, "/api/modules/**")
            	        .hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")

            	    .requestMatchers(HttpMethod.GET, "/api/lessons/**")
            	        .hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")

            	    // ❗ THEN restrict write operations
            	    .requestMatchers("/api/modules/**")
            	        .hasAnyRole("INSTRUCTOR", "ADMIN")

            	    .requestMatchers("/api/lessons/**")
            	        .hasAnyRole("INSTRUCTOR", "ADMIN")

            	    // Admin
            	    .requestMatchers("/api/courses/admin/**").hasRole("ADMIN")
            	    .requestMatchers("/api/admin/**").hasRole("ADMIN")

            	    // Instructor
            	    .requestMatchers("/api/courses/instructor/**").hasRole("INSTRUCTOR")
            	    .requestMatchers("/api/instructor/**").hasRole("INSTRUCTOR")
            	    .requestMatchers(HttpMethod.GET, "/api/assignments/**")
            	    .hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")
            	.requestMatchers("/api/assignments/**")
            	    .hasAnyRole("INSTRUCTOR", "ADMIN")

            	// Submissions
            	    .requestMatchers(HttpMethod.GET, "/api/submissions/my")
            	    .hasRole("STUDENT")
            	.requestMatchers(HttpMethod.POST, "/api/submissions")
            	    .hasRole("STUDENT")
            	.requestMatchers(HttpMethod.DELETE, "/api/submissions/*")   // ✅ add this
            	    .hasRole("STUDENT")
            	.requestMatchers(HttpMethod.GET, "/api/submissions/assignment/**")
            	    .hasAnyRole("INSTRUCTOR", "ADMIN")
            	.requestMatchers(HttpMethod.PUT, "/api/submissions/*/grade")
            	    .hasAnyRole("INSTRUCTOR", "ADMIN")

            	// Upload
            	.requestMatchers("/api/upload").authenticated()
            	    // Student
            	    .requestMatchers("/api/student/**").hasRole("STUDENT")

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
    public MultipartFilter multipartFilter() {
        return new MultipartFilter();
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