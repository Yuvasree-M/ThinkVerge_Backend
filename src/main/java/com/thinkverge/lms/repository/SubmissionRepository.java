package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.Submission;
import com.thinkverge.lms.model.Assignment;
import com.thinkverge.lms.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByAssignment(Assignment assignment);

    List<Submission> findByStudent(User student);

    Optional<Submission> findByAssignmentAndStudent(Assignment assignment, User student);

    boolean existsByAssignmentAndStudent(Assignment assignment, User student);
    
    @Query("SELECT s FROM Submission s JOIN FETCH s.assignment WHERE s.student = :student")
    List<Submission> findByStudentWithAssignment(@Param("student") User student);
}