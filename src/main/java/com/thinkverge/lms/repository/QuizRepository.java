package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.Quiz;
import com.thinkverge.lms.model.Course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByCourse(Course course);
}