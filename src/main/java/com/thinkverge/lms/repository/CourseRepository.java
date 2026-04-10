package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.Course;
import com.thinkverge.lms.model.User;
import com.thinkverge.lms.enums.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByStatus(CourseStatus status);

    List<Course> findByInstructor(User instructor);
    List<Course> findByInstructorEmail(String email);
    List<Course> findByStatusAndInstructor(CourseStatus status, User instructor);
}