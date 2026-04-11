package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.request.CourseModuleRequest;
import com.thinkverge.lms.dto.request.CourseModuleUpdateRequest;
import com.thinkverge.lms.dto.response.CourseModuleResponse;
import com.thinkverge.lms.model.CourseModule;
import com.thinkverge.lms.service.CourseModuleService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class CourseModuleController {

    private final CourseModuleService moduleService;

    @PostMapping
    public CourseModule create(@RequestBody CourseModuleRequest request) {

        String email =
            SecurityContextHolder.getContext().getAuthentication().getName();

        return moduleService.create(request, email);
    }
    @GetMapping("/course/{courseId}")
    public List<CourseModuleResponse> getByCourse(@PathVariable Long courseId) {
        return moduleService.getByCourse(courseId);
    }
    @PutMapping("/{id}")
    public CourseModule update(@PathVariable Long id,
                               @RequestBody CourseModuleUpdateRequest req) {
        return moduleService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        moduleService.delete(id);
    }
}