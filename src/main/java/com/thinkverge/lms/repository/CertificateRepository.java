package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.Certificate;
import com.thinkverge.lms.model.User;
import com.thinkverge.lms.model.Course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    // Eagerly fetch course + instructor so getName() works without LazyInit exception
    @Query("SELECT c FROM Certificate c " +
           "JOIN FETCH c.student " +
           "JOIN FETCH c.course co " +
           "JOIN FETCH co.instructor " +
           "WHERE c.student = :student")
    List<Certificate> findByStudentWithDetails(@Param("student") User student);

    @Query("SELECT c FROM Certificate c " +
           "JOIN FETCH c.student " +
           "JOIN FETCH c.course co " +
           "JOIN FETCH co.instructor " +
           "WHERE c.id = :id")
    Optional<Certificate> findByIdWithDetails(@Param("id") Long id);

    List<Certificate> findByStudent(User student);

    Optional<Certificate> findByStudentAndCourse(User student, Course course);
}
