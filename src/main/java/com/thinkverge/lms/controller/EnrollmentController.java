package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.response.EnrollmentResponse;
import com.thinkverge.lms.service.EnrollmentService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // student request
    @PostMapping("/{courseId}/request")
    public void request(
            @PathVariable Long courseId,
            Authentication auth
    ) {
        enrollmentService.requestEnrollment(
                courseId,
                auth.getName()
        );
    }
    @GetMapping("/instructor/all")
    public List<EnrollmentResponse> all(Authentication auth) {
        return enrollmentService.allInstructorEnrollments(auth.getName());
    }
    // instructor approve
    @PutMapping("/{id}/approve")
    public void approve(@PathVariable Long id) {
        enrollmentService.approve(id);
    }

    // instructor reject
    @PutMapping("/{id}/reject")
    public void reject(@PathVariable Long id) {
        enrollmentService.reject(id);
    }

    // student my enrollments
    @GetMapping("/my")
    public List<EnrollmentResponse> my(
            Authentication auth
    ) {
        return enrollmentService.myEnrollments(
                auth.getName()
        );
    }

    // instructor pending
    @GetMapping("/instructor/pending")
    public List<EnrollmentResponse> pending(
            Authentication auth
    ) {
        return enrollmentService.pendingRequests(
                auth.getName()
        );
    }
}