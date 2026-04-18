package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.request.GradeRequest;
import com.thinkverge.lms.dto.request.SubmissionRequest;
import com.thinkverge.lms.dto.response.SubmissionResponse;
import com.thinkverge.lms.service.SubmissionService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    // student submit
    @PostMapping
    public void submit(
            @RequestBody SubmissionRequest request,
            Authentication auth
    ) {
        submissionService.submit(request, auth.getName());
    }

    // instructor grade
    @PutMapping("/{id}/grade")
    public void grade(
            @PathVariable Long id,
            @RequestBody GradeRequest request
    ) {
        submissionService.grade(id, request);
    }

    // student my submissions
    @GetMapping("/my")
    public List<SubmissionResponse> my(
            Authentication auth
    ) {
        return submissionService.mySubmissions(auth.getName());
    }

    // instructor view assignment submissions
    @GetMapping("/assignment/{assignmentId}")
    public List<SubmissionResponse> byAssignment(
            @PathVariable Long assignmentId
    ) {
        return submissionService.byAssignment(assignmentId);
    }
    
 // student delete (only if not graded)
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id,
            Authentication auth
    ) {
        submissionService.delete(id, auth.getName());
    }
}