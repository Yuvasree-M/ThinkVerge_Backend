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
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Public ───────────────────────────────────────────
            	
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/public/feedback").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/courses").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/courses/{id:[0-9]+}").permitAll()

                // ── Courses: specific sub-paths FIRST (before generic GET) ──
                .requestMatchers("/api/courses/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/courses/instructor/**").hasRole("INSTRUCTOR")
                .requestMatchers(HttpMethod.GET, "/api/courses/**")
                    .hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/courses/**")
                    .hasAnyRole("INSTRUCTOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/courses/**")
                    .hasAnyRole("INSTRUCTOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/courses/**")
                    .hasAnyRole("INSTRUCTOR", "ADMIN")

                // ── Modules ──────────────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/modules/**")
                    .hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")
                .requestMatchers("/api/modules/**")
                    .hasAnyRole("INSTRUCTOR", "ADMIN")

                // ── Lessons ──────────────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/lessons/**")
                    .hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")
                .requestMatchers("/api/lessons/**")
                    .hasAnyRole("INSTRUCTOR", "ADMIN")

                // ── Admin ────────────────────────────────────────────
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,  "/api/users/pending").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/api/users/*/approve").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/api/public/feedback/*/approve").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/public/feedback/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/public/feedback/pending").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users/profile-image")
                .hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")

                .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")

                // ── Instructor ───────────────────────────────────────
                .requestMatchers("/api/instructor/**").hasRole("INSTRUCTOR")

                // ── Assignments ──────────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/assignments/**")
                    .hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")
                .requestMatchers("/api/assignments/**")
                    .hasAnyRole("INSTRUCTOR", "ADMIN")

                // ── Submissions ──────────────────────────────────────
                // Student-only: specific routes FIRST
                .requestMatchers(HttpMethod.GET, "/api/submissions/my")
                    .hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/submissions")
                    .hasRole("STUDENT")
                .requestMatchers(HttpMethod.DELETE, "/api/submissions/*")
                    .hasRole("STUDENT")
                // Instructor/Admin: assignment submissions
                .requestMatchers(HttpMethod.GET, "/api/submissions/assignment/**")
                    .hasAnyRole("INSTRUCTOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/submissions/*/grade")
                    .hasAnyRole("INSTRUCTOR", "ADMIN")

                // ── Quizzes ──────────────────────────────────────────
                // IMPORTANT: more-specific paths first
                // Instructor endpoint includes "/instructor" suffix
                .requestMatchers(HttpMethod.GET, "/api/quizzes/module/*/instructor")
                    .hasAnyRole("INSTRUCTOR", "ADMIN")
                // Module statuses for student
                .requestMatchers(HttpMethod.GET, "/api/quizzes/module-status/**")
                    .hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")
                // Generic module quiz (student — no correct answers)
                .requestMatchers(HttpMethod.GET, "/api/quizzes/module/**")
                    .hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")
                // Final quiz — instructor (with answers)
                .requestMatchers(HttpMethod.GET, "/api/quizzes/final/course/*/instructor")
                    .hasAnyRole("INSTRUCTOR", "ADMIN")
                // Final quiz — student (no answers)
                .requestMatchers(HttpMethod.GET, "/api/quizzes/final/**")
                    .hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")
                // Instructor: create final quiz
                .requestMatchers(HttpMethod.POST, "/api/quizzes/final")
                    .hasAnyRole("INSTRUCTOR", "ADMIN")
                // Student: get my quiz attempts
                .requestMatchers(HttpMethod.GET, "/api/quizzes/my/**")
                    .hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")
                // Student: submit quiz
                .requestMatchers(HttpMethod.POST, "/api/quizzes/submit")
                    .hasRole("STUDENT")
                // Instructor: create module quiz / delete quiz
                .requestMatchers(HttpMethod.POST, "/api/quizzes")
                    .hasAnyRole("INSTRUCTOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/quizzes/**")
                    .hasAnyRole("INSTRUCTOR", "ADMIN")

                // ── Enrollments ──────────────────────────────────────
                .requestMatchers("/api/enrollments/**").authenticated()

                // ── Progress ─────────────────────────────────────────
                .requestMatchers("/api/progress/**").authenticated()

                // ── Certificates ─────────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/certificates/{id:[0-9]+}").permitAll()
                .requestMatchers("/api/certificates/**").hasRole("STUDENT")

                // ── Upload ───────────────────────────────────────────
                .requestMatchers("/api/upload").authenticated()

                // ── Student ──────────────────────────────────────────
                .requestMatchers("/api/student/**").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/public/feedback/approved").permitAll()
            	.requestMatchers("/actuator/health").permitAll()
            	.requestMatchers("/api/users/profile-image").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
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
        configuration.setAllowCredentials(true);

        configuration.setAllowedOriginPatterns(List.of(
        	    "http://localhost:5173",
        	    "https://thinkverge.netlify.app"
        	));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
