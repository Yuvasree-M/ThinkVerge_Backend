package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.request.AssignmentRequest;
import com.thinkverge.lms.dto.response.AssignmentResponse;
import com.thinkverge.lms.service.AssignmentService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    // create
    @PostMapping
    public void create(@RequestBody AssignmentRequest request) {
        assignmentService.create(request);
    }

    // update
    @PutMapping("/{id}")
    public void update(
            @PathVariable Long id,
            @RequestBody AssignmentRequest request
    ) {
        assignmentService.update(id, request);
    }

    // delete
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        assignmentService.delete(id);
    }

    // get by course
    @GetMapping("/course/{courseId}")
    public List<AssignmentResponse> byCourse(
            @PathVariable Long courseId
    ) {
        return assignmentService.getByCourse(courseId);
    }
}