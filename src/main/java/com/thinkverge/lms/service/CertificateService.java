//package com.thinkverge.lms.service;
//
//import com.thinkverge.lms.dto.response.CertificateResponse;
//import com.thinkverge.lms.model.*;
//import com.thinkverge.lms.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class CertificateService {
//
//    private final CertificateRepository certificateRepository;
//    private final UserRepository userRepository;
//    private final QuizAttemptRepository attemptRepository;
//    private final QuizRepository quizRepository;
//    private final CourseModuleRepository moduleRepository;
//
//    // Student: get my certificates
//    public List<CertificateResponse> myCertificates(String email) {
//        User student = userRepository.findByEmail(email).orElseThrow();
//        return certificateRepository.findByStudent(student)
//                .stream()
//                .map(c -> map(c, student))
//                .collect(Collectors.toList());
//    }
//
//    // Public: get a single certificate by ID (for the certificate view page)
//    public CertificateResponse getById(Long id) {
//        Certificate c = certificateRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate not found"));
//        return map(c, c.getStudent());
//    }
//
//    private CertificateResponse map(Certificate c, User student) {
//        Integer avgScore = computeAverageScore(student, c.getCourse());
//        String grade     = gradeLabel(avgScore);
//
//        return CertificateResponse.builder()
//                .id(c.getId())
//                .courseTitle(c.getCourse().getTitle())
//                .studentName(c.getStudent().getName())
//                .instructorName(c.getCourse().getInstructor() != null
//                        ? c.getCourse().getInstructor().getName() : "ThinkVerge Instructor")
//                .certificateUrl(c.getCertificateUrl())
//                .issuedAt(c.getIssuedAt())
//                .averageScore(avgScore)
//                .gradeLabel(grade)
//                .build();
//    }
//
//    /**
//     * Average the best (highest) passed attempt score for every quiz in the course
//     * (module quizzes + final quiz). Uses best score per quiz so retakes don't
//     * penalise a student who improved.
//     */
//    private Integer computeAverageScore(User student, Course course) {
//        // Collect all quizzes: module quizzes + final quiz
//        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndex(course.getId());
//
//        // module quizzes
//        List<Quiz> quizzes = modules.stream()
//                .map(mod -> quizRepository.findByModule(mod))
//                .filter(java.util.Optional::isPresent)
//                .map(java.util.Optional::get)
//                .collect(Collectors.toList());
//
//        // final quiz
//        quizRepository.findByCourseAndIsFinalQuizTrue(course).ifPresent(quizzes::add);
//
//        if (quizzes.isEmpty()) return null;
//
//        // For each quiz take the best attempt score
//        double total = 0;
//        int count = 0;
//        for (Quiz quiz : quizzes) {
//            List<QuizAttempt> attempts = attemptRepository.findByQuizAndStudent(quiz, student);
//            if (attempts.isEmpty()) continue;
//            int best = attempts.stream().mapToInt(QuizAttempt::getScore).max().getAsInt();
//            total += best;
//            count++;
//        }
//
//        return count > 0 ? (int) Math.round(total / count) : null;
//    }
//
//    /** Grade label based on average score */
//    private String gradeLabel(Integer score) {
//        if (score == null) return "Pass";
//        if (score >= 90)   return "Distinction";
//        if (score >= 75)   return "Merit";
//        return "Pass";
//    }
//}


package com.thinkverge.lms.service;

import com.thinkverge.lms.dto.response.CertificateResponse;
import com.thinkverge.lms.model.*;
import com.thinkverge.lms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository   certificateRepository;
    private final UserRepository          userRepository;
    private final QuizAttemptRepository   attemptRepository;
    private final QuizRepository          quizRepository;
    private final CourseModuleRepository  moduleRepository;

    // Student: get my certificates
    @Transactional(readOnly = true)
    public List<CertificateResponse> myCertificates(String email) {
        User student = userRepository.findByEmail(email).orElseThrow();
        return certificateRepository.findByStudentWithDetails(student)
                .stream()
                .map(c -> map(c, student))
                .collect(Collectors.toList());
    }

    // Public: get a single certificate by ID
    @Transactional(readOnly = true)
    public CertificateResponse getById(Long id) {
        Certificate c = certificateRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Certificate not found"));
        return map(c, c.getStudent());
    }

    // ── Mapper ───────────────────────────────────────────────
    private CertificateResponse map(Certificate c, User student) {
        String instructorName = "ThinkVerge Instructor";
        try {
            if (c.getCourse().getInstructor() != null) {
                instructorName = c.getCourse().getInstructor().getName();
            }
        } catch (Exception ignored) {}

        Integer avgScore = computeAverageScore(student, c.getCourse());
        String  grade    = gradeLabel(avgScore);

        return CertificateResponse.builder()
                .id(c.getId())
                .courseTitle(c.getCourse().getTitle())
                .studentName(c.getStudent().getName())
                .instructorName(instructorName)
                .certificateUrl(c.getCertificateUrl())
                .issuedAt(c.getIssuedAt())
                .averageScore(avgScore)
                .gradeLabel(grade)
                .build();
    }

    /**
     * Average of the best attempt score per quiz (module quizzes + final quiz).
     */
    private Integer computeAverageScore(User student, Course course) {
        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndex(course.getId());

        List<Quiz> quizzes = modules.stream()
                .map(mod -> quizRepository.findByModule(mod))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toList());

        quizRepository.findByCourseAndIsFinalQuizTrue(course).ifPresent(quizzes::add);

        if (quizzes.isEmpty()) return null;

        double total = 0;
        int    count = 0;
        for (Quiz quiz : quizzes) {
            List<QuizAttempt> attempts = attemptRepository.findByQuizAndStudent(quiz, student);
            if (attempts.isEmpty()) continue;
            int best = attempts.stream().mapToInt(QuizAttempt::getScore).max().getAsInt();
            total += best;
            count++;
        }
        return count > 0 ? (int) Math.round(total / count) : null;
    }

    private String gradeLabel(Integer score) {
        if (score == null) return "Pass";
        if (score >= 90)   return "Distinction";
        if (score >= 75)   return "Merit";
        return "Pass";
    }
}
