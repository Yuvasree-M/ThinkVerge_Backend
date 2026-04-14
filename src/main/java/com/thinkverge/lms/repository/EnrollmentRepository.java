package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.Enrollment;
import com.thinkverge.lms.model.User;
import com.thinkverge.lms.model.Course;
import com.thinkverge.lms.enums.EnrollmentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudent(User student);

    List<Enrollment> findByCourse(Course course);

    List<Enrollment> findByStatus(EnrollmentStatus status);

    Optional<Enrollment> findByStudentAndCourse(User student, Course course);

    boolean existsByStudentAndCourse(User student, Course course);

    List<Enrollment> findByStudentAndStatus(User student, EnrollmentStatus status);
    
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course WHERE e.student = :student")
    List<Enrollment> findByStudentWithCourse(@Param("student") User student);
}