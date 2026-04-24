package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.response.EnrollmentResponse;
import com.thinkverge.lms.dto.response.ProgressResponse;
import com.thinkverge.lms.dto.response.StudentDashboardResponse;
import com.thinkverge.lms.dto.response.SubmissionResponse;
import com.thinkverge.lms.model.*;
import com.thinkverge.lms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final EnrollmentRepository enrollmentRepo;
    private final LessonProgressRepository progressRepo;
    private final SubmissionRepository submissionRepo;
    private final UserRepository userRepo;

    @GetMapping("/student")
    @Transactional(readOnly = true)
    public ResponseEntity<StudentDashboardResponse> getStudentDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {

        User student = userRepo.findByEmail(userDetails.getUsername()).orElseThrow();

        List<Enrollment>      enrollments = enrollmentRepo.findByStudentWithCourse(student);
        List<LessonProgress>  progress    = progressRepo.findByStudentWithLesson(student);
        List<Submission>      submissions = submissionRepo.findByStudent(student);

        return ResponseEntity.ok(StudentDashboardResponse.builder()
                .enrollments(enrollments.stream().map(this::toEnrollmentDto).toList())
                .progress(progress.stream().map(this::toProgressDto).toList())
                .submissions(submissions.stream().map(this::toSubmissionDto).toList())
                .build());
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private EnrollmentResponse toEnrollmentDto(Enrollment e) {
        return EnrollmentResponse.builder()
                .id(e.getId())
                .status(e.getStatus().name())
                .requestedAt(e.getEnrolledAt())
                .course(EnrollmentResponse.CourseDto.builder()
                        .id(e.getCourse().getId())
                        .title(e.getCourse().getTitle())
                        .thumbnail(e.getCourse().getThumbnail())
                        .category(e.getCourse().getCategory())
                        .build())
                .student(EnrollmentResponse.StudentDto.builder()
                        .name(e.getStudent().getName())
                        .email(e.getStudent().getEmail())
                        .build())
                .build();
    }

    private ProgressResponse toProgressDto(LessonProgress p) {
        Lesson lesson = p.getLesson();
        Long courseId = null;
        try {
            courseId = lesson.getModule().getCourse().getId();
        } catch (Exception ignored) {}

        return ProgressResponse.builder()
                .lessonId(lesson.getId())
                .lessonTitle(lesson.getTitle())
                .lessonType(lesson.getType() != null ? lesson.getType().name() : null)
                .courseId(courseId)
                .completed(p.getCompleted())
                .percentage(p.getCompletionPercentage())
                .build();
    }

    private SubmissionResponse toSubmissionDto(Submission s) {
        Course course = s.getAssignment().getCourse();
        return SubmissionResponse.builder()
                .id(s.getId())
                .assignmentId(s.getAssignment().getId())
                .assignmentTitle(s.getAssignment().getTitle())
                .courseId(course != null ? course.getId() : null)
                .courseTitle(course != null ? course.getTitle() : null)
                .studentName(s.getStudent().getName())
                .fileUrl(s.getFileUrl())
                .content(s.getContent())
                .grade(s.getMarks())
                .feedback(s.getFeedback())
                .status(s.getStatus() != null ? s.getStatus().name() : null)
                .submittedAt(s.getSubmittedAt())
                .gradedAt(s.getGradedAt())
                .build();
    }
}