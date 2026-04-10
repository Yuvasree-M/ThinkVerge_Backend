package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.QuizQuestion;
import com.thinkverge.lms.model.Quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    List<QuizQuestion> findByQuiz(Quiz quiz);
}