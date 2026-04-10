package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseModuleRepository 
        extends JpaRepository<CourseModule, Long> {

    List<CourseModule> findByCourseIdOrderByOrderIndex(Long courseId);

}