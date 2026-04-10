package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.Certificate;
import com.thinkverge.lms.model.User;
import com.thinkverge.lms.model.Course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    List<Certificate> findByStudent(User student);

    Optional<Certificate> findByStudentAndCourse(User student, Course course);
}