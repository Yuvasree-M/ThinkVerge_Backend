package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.response.InstructorPublicResponse;
import com.thinkverge.lms.dto.response.TestimonialResponse;
import com.thinkverge.lms.enums.Role;
import com.thinkverge.lms.model.*;
import com.thinkverge.lms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class LandingController {

    private final com.thinkverge.lms.repository.UserRepository       userRepository;
    private final com.thinkverge.lms.repository.CourseRepository      courseRepository;
    private final com.thinkverge.lms.repository.EnrollmentRepository  enrollmentRepository;
    private final com.thinkverge.lms.repository.CertificateRepository certificateRepository;
    private final com.thinkverge.lms.repository.QuizAttemptRepository attemptRepository;
    private final com.thinkverge.lms.repository.QuizRepository        quizRepository;
    private final com.thinkverge.lms.repository.CourseModuleRepository moduleRepository;

    // ── Public: list instructors ──────────────────────────────
    @GetMapping("/instructors")
    @Transactional(readOnly = true)
    public List<InstructorPublicResponse> instructors() {
        List<User> instructors = userRepository.findByRole(Role.INSTRUCTOR);
        return instructors.stream().map(inst -> {
            List<Course> courses = courseRepository.findByInstructor(inst);
            int studentCount = courses.stream()
                    .mapToInt(c -> enrollmentRepository.findByCourse(c).size())
                    .sum();
            // derive specialty from first course category or fallback
            String specialty = courses.stream()
                    .map(Course::getCategory)
                    .filter(cat -> cat != null && !cat.isBlank())
                    .findFirst()
                    .orElse("Online Education");

            return InstructorPublicResponse.builder()
                    .id(inst.getId())
                    .name(inst.getName())
                    .email(inst.getEmail())
                    .profileImage(inst.getProfileImage())
                    .courseCount(courses.size())
                    .studentCount(studentCount)
                    .specialty(specialty)
                    .build();
        }).collect(Collectors.toList());
    }

    // ── Public: testimonials from certificate holders ─────────
    @GetMapping("/testimonials")
    @Transactional(readOnly = true)
    public List<TestimonialResponse> testimonials() {
        // Use students who have earned certificates as real testimonials
        List<Certificate> certs = certificateRepository.findAll();

        return certs.stream().map(c -> {
            // Compute average quiz score for this student+course
            Integer avg = computeAvgScore(c.getStudent(), c.getCourse());
            String grade = gradeLabel(avg);
            String dateStr = c.getIssuedAt() != null
                    ? c.getIssuedAt().format(DateTimeFormatter.ofPattern("MMM yyyy"))
                    : "";

            return TestimonialResponse.builder()
                    .id(c.getId())
                    .studentName(c.getStudent().getName())
                    .profileImage(c.getStudent().getProfileImage())
                    .courseTitle(c.getCourse().getTitle())
                    .gradeLabel(grade)
                    .averageScore(avg)
                    .issuedAt(dateStr)
                    .build();
        }).collect(Collectors.toList());
    }

    private Integer computeAvgScore(User student, Course course) {
        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndex(course.getId());
        List<Quiz> quizzes = new ArrayList<>();
        for (CourseModule mod : modules) {
            quizRepository.findByModule(mod).ifPresent(quizzes::add);
        }
        quizRepository.findByCourseAndIsFinalQuizTrue(course).ifPresent(quizzes::add);
        if (quizzes.isEmpty()) return null;
        double total = 0; int count = 0;
        for (Quiz q : quizzes) {
            List<QuizAttempt> attempts = attemptRepository.findByQuizAndStudent(q, student);
            if (attempts.isEmpty()) continue;
            int best = attempts.stream().mapToInt(QuizAttempt::getScore).max().getAsInt();
            total += best; count++;
        }
        return count > 0 ? (int) Math.round(total / count) : null;
    }

    private String gradeLabel(Integer score) {
        if (score == null) return "Completed";
        if (score >= 90) return "Distinction";
        if (score >= 75) return "Merit";
        return "Pass";
    }
}
