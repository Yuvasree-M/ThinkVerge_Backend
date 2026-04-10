package com.thinkverge.lms.service;

import com.thinkverge.lms.dto.response.EnrollmentResponse;
import com.thinkverge.lms.enums.EnrollmentStatus;
import com.thinkverge.lms.model.*;
import com.thinkverge.lms.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EmailService emailService;

    // STUDENT REQUEST ENROLLMENT
    public void requestEnrollment(Long courseId, String email) {

        User student = userRepository.findByEmail(email).orElseThrow();
        Course course = courseRepository.findById(courseId).orElseThrow();

        // already enrolled
        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new RuntimeException("Already requested");
        }

        // one active course rule
        List<Enrollment> active = enrollmentRepository
                .findByStudentAndStatus(student, EnrollmentStatus.APPROVED);

        if (!active.isEmpty()) {
            throw new RuntimeException("Only one active course allowed");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.PENDING)
                .enrolledAt(LocalDateTime.now())
                .build();

        enrollmentRepository.save(enrollment);

        // EMAIL → Instructor
        emailService.sendEnrollmentRequested(
                course.getInstructor().getEmail(),
                student.getName(),
                course.getTitle()
        );
    }
    public List<EnrollmentResponse> allInstructorEnrollments(String email) {
        return enrollmentRepository.findAll()
                .stream()
                .filter(e -> e.getCourse().getInstructor().getEmail().equals(email))
                .map(this::mapToResponse)
                .toList();
    }
    // INSTRUCTOR APPROVE
    public void approve(Long enrollmentId) {

        Enrollment enrollment = enrollmentRepository
                .findById(enrollmentId)
                .orElseThrow();

        enrollment.setStatus(EnrollmentStatus.APPROVED);
        enrollment.setEnrolledAt(LocalDateTime.now());

        enrollmentRepository.save(enrollment);

        // EMAIL → Student
        emailService.sendEnrollmentApproved(
                enrollment.getStudent().getEmail(),
                enrollment.getCourse().getTitle()
        );
    }

    // INSTRUCTOR REJECT
    public void reject(Long enrollmentId) {

        Enrollment enrollment = enrollmentRepository
                .findById(enrollmentId)
                .orElseThrow();

        enrollment.setStatus(EnrollmentStatus.REJECTED);

        enrollmentRepository.save(enrollment);

        // EMAIL → Student
        emailService.send(
                enrollment.getStudent().getEmail(),
                "Enrollment Rejected",
                "Your enrollment for course '"
                        + enrollment.getCourse().getTitle()
                        + "' was rejected."
        );
    }

    // STUDENT MY ENROLLMENTS
    public List<EnrollmentResponse> myEnrollments(String email) {

        User student = userRepository.findByEmail(email).orElseThrow();

        return enrollmentRepository
                .findByStudent(student)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // INSTRUCTOR PENDING REQUESTS
    public List<EnrollmentResponse> pendingRequests(String instructorEmail) {

        return enrollmentRepository
                .findByStatus(EnrollmentStatus.PENDING)
                .stream()
                .filter(e -> e.getCourse()
                        .getInstructor()
                        .getEmail()
                        .equals(instructorEmail))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private EnrollmentResponse mapToResponse(Enrollment e) {
        return EnrollmentResponse.builder()
                .id(e.getId())
                .status(e.getStatus().name())
                .requestedAt(e.getEnrolledAt())

                .course(
                        EnrollmentResponse.CourseDto.builder()
                                .id(e.getCourse().getId())
                                .title(e.getCourse().getTitle())
                                .build()
                )

                .student(
                        EnrollmentResponse.StudentDto.builder()
                                .name(e.getStudent().getName())
                                .email(e.getStudent().getEmail())
                                .build()
                )

                .build();
    }
}