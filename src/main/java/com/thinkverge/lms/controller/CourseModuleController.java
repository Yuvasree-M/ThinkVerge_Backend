package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.request.CourseModuleRequest;
import com.thinkverge.lms.model.CourseModule;
import com.thinkverge.lms.service.CourseModuleService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class CourseModuleController {

    private final CourseModuleService moduleService;

    @PostMapping
    public CourseModule create(
            @RequestBody CourseModuleRequest request
    ) {
        return moduleService.create(request);
    }

    @GetMapping("/course/{courseId}")
    public List<CourseModule> getByCourse(
            @PathVariable Long courseId
    ) {
        return moduleService.getByCourse(courseId);
    }
}