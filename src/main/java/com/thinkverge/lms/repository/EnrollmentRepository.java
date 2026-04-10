package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.Enrollment;
import com.thinkverge.lms.model.User;
import com.thinkverge.lms.model.Course;
import com.thinkverge.lms.enums.EnrollmentStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudent(User student);

    List<Enrollment> findByCourse(Course course);

    List<Enrollment> findByStatus(EnrollmentStatus status);

    Optional<Enrollment> findByStudentAndCourse(User student, Course course);

    boolean existsByStudentAndCourse(User student, Course course);

    List<Enrollment> findByStudentAndStatus(User student, EnrollmentStatus status);
}