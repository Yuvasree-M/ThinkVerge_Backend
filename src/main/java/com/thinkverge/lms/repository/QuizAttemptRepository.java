package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.QuizAttempt;
import com.thinkverge.lms.model.Quiz;
import com.thinkverge.lms.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByQuiz(Quiz quiz);

    List<QuizAttempt> findByStudent(User student);

    List<QuizAttempt> findByQuizAndStudent(Quiz quiz, User student);
}