package com.thinkverge.lms.service;

import com.thinkverge.lms.dto.request.CourseModuleRequest;
import com.thinkverge.lms.model.Course;
import com.thinkverge.lms.model.CourseModule;
import com.thinkverge.lms.repository.CourseModuleRepository;
import com.thinkverge.lms.repository.CourseRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseModuleService {

    private final CourseModuleRepository moduleRepository;
    private final CourseRepository courseRepository;

    public CourseModule create(CourseModuleRequest request) {

        Course course = courseRepository
                .findById(request.getCourseId())
                .orElseThrow();

        CourseModule module = CourseModule.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .orderIndex(request.getOrderIndex())
                .course(course)
                .build();

        return moduleRepository.save(module);
    }

    public List<CourseModule> getByCourse(Long courseId) {
        return moduleRepository
                .findByCourseIdOrderByOrderIndex(courseId);
    }
}