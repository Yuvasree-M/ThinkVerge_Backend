package com.thinkverge.lms.service;

import com.thinkverge.lms.dto.request.AssignmentRequest;
import com.thinkverge.lms.dto.response.AssignmentResponse;
import com.thinkverge.lms.model.*;
import com.thinkverge.lms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EmailService emailService;

    public void create(AssignmentRequest request) {
        Course course = courseRepository.findById(request.getCourseId()).orElseThrow();

        Assignment assignment = Assignment.builder()
                .course(course)
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .maxMarks(request.getMaxMarks())
                .pdfUrl(request.getPdfUrl())   // ✅
                .build();

        Assignment saved = assignmentRepository.save(assignment);

        List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);
        for (Enrollment e : enrollments) {
            emailService.sendNewAssignment(
                    e.getStudent().getEmail(),
                    saved.getTitle(),
                    course.getTitle()
            );
        }
    }

    public void update(Long id, AssignmentRequest request) {
        Assignment assignment = assignmentRepository.findById(id).orElseThrow();
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDueDate(request.getDueDate());
        assignment.setMaxMarks(request.getMaxMarks());
        if (request.getPdfUrl() != null) {
            assignment.setPdfUrl(request.getPdfUrl()); // ✅ allow PDF update
        }
        assignmentRepository.save(assignment);
    }

    public void delete(Long id) {
        assignmentRepository.deleteById(id);
    }

    public List<AssignmentResponse> getByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        return assignmentRepository.findByCourse(course)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private AssignmentResponse mapToResponse(Assignment a) {
        return AssignmentResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .description(a.getDescription())
                .dueDate(a.getDueDate())
                .maxPoints(a.getMaxMarks())   // ✅ renamed to match frontend
                .pdfUrl(a.getPdfUrl())         // ✅
                .build();
    }
}