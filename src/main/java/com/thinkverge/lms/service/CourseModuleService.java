package com.thinkverge.lms.service;

import com.thinkverge.lms.dto.request.CourseModuleRequest;
import com.thinkverge.lms.dto.request.CourseModuleUpdateRequest;
import com.thinkverge.lms.dto.response.CourseModuleResponse;
import com.thinkverge.lms.model.Course;
import com.thinkverge.lms.model.CourseModule;
import com.thinkverge.lms.repository.CourseModuleRepository;
import com.thinkverge.lms.repository.CourseRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseModuleService {

    private final CourseModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    public CourseModule create(CourseModuleRequest request, String email) {

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getInstructor().getEmail().equals(email)) {
            throw new RuntimeException("Not your course");
        }

        CourseModule module = CourseModule.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .orderIndex(request.getOrderIndex())
                .course(course)
                .createdAt(LocalDateTime.now())
                .build();

        return moduleRepository.save(module);
    }
    public List<CourseModuleResponse> getByCourse(Long courseId) {

        List<CourseModule> modules =
                moduleRepository.findByCourseIdOrderByOrderIndex(courseId);

        return modules.stream().map(m ->
                CourseModuleResponse.builder()
                        .id(m.getId())
                        .title(m.getTitle())
                        .description(m.getDescription())
                        .orderIndex(m.getOrderIndex())
                        .build()
        ).toList();
    }
    
    public CourseModule update(Long id, CourseModuleUpdateRequest req) {
        CourseModule module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        module.setTitle(req.getTitle());
        module.setDescription(req.getDescription());
        module.setOrderIndex(req.getOrderIndex());

        return moduleRepository.save(module);
    }

    public void delete(Long id) {
    	moduleRepository.deleteById(id);
    }
   
}