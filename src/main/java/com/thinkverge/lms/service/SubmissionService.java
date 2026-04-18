//package com.thinkverge.lms.service;
//
//import com.thinkverge.lms.dto.request.GradeRequest;
//import com.thinkverge.lms.dto.request.SubmissionRequest;
//import com.thinkverge.lms.dto.response.SubmissionResponse;
//import com.thinkverge.lms.enums.SubmissionStatus;
//import com.thinkverge.lms.model.*;
//import com.thinkverge.lms.repository.*;
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class SubmissionService {
//
//    private final SubmissionRepository submissionRepository;
//    private final AssignmentRepository assignmentRepository;
//    private final UserRepository userRepository;
//    private final EmailService emailService;
//
//    public void submit(SubmissionRequest request, String email) {
//        User student = userRepository.findByEmail(email).orElseThrow();
//        Assignment assignment = assignmentRepository
//                .findById(request.getAssignmentId()).orElseThrow();
//
//        Submission submission = submissionRepository
//                .findByAssignmentAndStudent(assignment, student)
//                .orElse(Submission.builder()
//                        .assignment(assignment)
//                        .student(student)
//                        .build());
//
//        submission.setFileUrl(request.getFileUrl());
//        submission.setContent(request.getContent());         // ✅
//        submission.setSubmittedAt(LocalDateTime.now());
//
//        // ✅ null-safe due date check
//        if (assignment.getDueDate() != null &&
//            LocalDateTime.now().isAfter(assignment.getDueDate())) {
//            submission.setStatus(SubmissionStatus.LATE);
//        } else {
//            submission.setStatus(SubmissionStatus.SUBMITTED);
//        }
//
//        submissionRepository.save(submission);
//
//        emailService.sendAssignmentSubmitted(
//                assignment.getCourse().getInstructor().getEmail(),
//                student.getName(),
//                assignment.getTitle()
//        );
//    }
//
//    public void grade(Long submissionId, GradeRequest request) {
//        Submission submission = submissionRepository.findById(submissionId).orElseThrow();
//        submission.setMarks(request.getGrade());             // ✅ grade → marks (DB)
//        submission.setFeedback(request.getFeedback());
//        submission.setGradedAt(LocalDateTime.now());
//        submissionRepository.save(submission);
//
//        emailService.sendAssignmentGraded(
//                submission.getStudent().getEmail(),
//                submission.getAssignment().getTitle(),
//                request.getGrade(),
//                request.getFeedback()
//        );
//    }
//
//    public List<SubmissionResponse> mySubmissions(String email) {
//        User student = userRepository.findByEmail(email).orElseThrow();
//        return submissionRepository.findByStudent(student)
//                .stream().map(this::map).collect(Collectors.toList());
//    }
//
//    public List<SubmissionResponse> byAssignment(Long assignmentId) {
//        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow();
//        return submissionRepository.findByAssignment(assignment)
//                .stream().map(this::map).collect(Collectors.toList());
//    }
//
//    private SubmissionResponse map(Submission s) {
//        return SubmissionResponse.builder()
//                .id(s.getId())
//                .assignmentId(s.getAssignment().getId())
//                .assignmentTitle(s.getAssignment().getTitle())   // ✅
//                .studentName(s.getStudent().getName())
//                .fileUrl(s.getFileUrl())
//                .grade(s.getMarks())                              // ✅ marks → grade
//                .feedback(s.getFeedback())
//                .status(s.getStatus() != null ? s.getStatus().name() : null)
//                .submittedAt(s.getSubmittedAt())
//                .build();
//    }
//    
//    public void delete(Long submissionId, String email) {
//        User student = userRepository.findByEmail(email).orElseThrow();
//        Submission submission = submissionRepository.findById(submissionId).orElseThrow();
//
//        if (!submission.getStudent().getId().equals(student.getId())) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your submission");
//        }
//        if (submission.getMarks() != null) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete a graded submission");
//        }
//        submissionRepository.deleteById(submissionId);
//    }
//}



package com.thinkverge.lms.service;

import com.thinkverge.lms.dto.request.GradeRequest;
import com.thinkverge.lms.dto.request.SubmissionRequest;
import com.thinkverge.lms.dto.response.SubmissionResponse;
import com.thinkverge.lms.enums.SubmissionStatus;
import com.thinkverge.lms.model.*;
import com.thinkverge.lms.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final QuizService quizService; // ✅ for certificate check after grading

    public void submit(SubmissionRequest request, String email) {
        User student = userRepository.findByEmail(email).orElseThrow();
        Assignment assignment = assignmentRepository
                .findById(request.getAssignmentId()).orElseThrow();

        Submission submission = submissionRepository
                .findByAssignmentAndStudent(assignment, student)
                .orElse(Submission.builder()
                        .assignment(assignment)
                        .student(student)
                        .build());

        submission.setFileUrl(request.getFileUrl());
        submission.setContent(request.getContent());
        submission.setSubmittedAt(LocalDateTime.now());

        if (assignment.getDueDate() != null &&
            LocalDateTime.now().isAfter(assignment.getDueDate())) {
            submission.setStatus(SubmissionStatus.LATE);
        } else {
            submission.setStatus(SubmissionStatus.SUBMITTED);
        }

        submissionRepository.save(submission);

        emailService.sendAssignmentSubmitted(
                assignment.getCourse().getInstructor().getEmail(),
                student.getName(),
                assignment.getTitle()
        );
    }

    public void grade(Long submissionId, GradeRequest request) {
        Submission submission = submissionRepository.findById(submissionId).orElseThrow();
        submission.setMarks(request.getGrade());
        submission.setFeedback(request.getFeedback());
        submission.setGradedAt(LocalDateTime.now());
        submissionRepository.save(submission);

        emailService.sendAssignmentGraded(
                submission.getStudent().getEmail(),
                submission.getAssignment().getTitle(),
                request.getGrade(),
                request.getFeedback()
        );

        // ✅ After grading, check if student now qualifies for certificate
        quizService.checkCertificateAfterGrade(
                submission.getStudent(),
                submission.getAssignment().getCourse()
        );
    }

    public List<SubmissionResponse> mySubmissions(String email) {
        User student = userRepository.findByEmail(email).orElseThrow();
        return submissionRepository.findByStudent(student)
                .stream().map(this::map).collect(Collectors.toList());
    }

    public List<SubmissionResponse> byAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow();
        return submissionRepository.findByAssignment(assignment)
                .stream().map(this::map).collect(Collectors.toList());
    }

    public void delete(Long submissionId, String email) {
        User student = userRepository.findByEmail(email).orElseThrow();
        Submission submission = submissionRepository.findById(submissionId).orElseThrow();

        if (!submission.getStudent().getId().equals(student.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your submission");
        }
        if (submission.getMarks() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot delete a graded submission");
        }
        submissionRepository.deleteById(submissionId);
    }

    private SubmissionResponse map(Submission s) {
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
