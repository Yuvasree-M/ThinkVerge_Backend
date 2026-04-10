package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.Assignment;
import com.thinkverge.lms.model.Course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByCourse(Course course);
}