package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.QuizAttempt;
import com.thinkverge.lms.model.Quiz;
import com.thinkverge.lms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByQuiz(Quiz quiz);

    List<QuizAttempt> findByStudent(User student);

    List<QuizAttempt> findByQuizAndStudent(Quiz quiz, User student);

    // ✅ best passed attempt — used for module unlock check
    Optional<QuizAttempt> findTopByQuizAndStudentAndPassedTrueOrderByScoreDesc(
            Quiz quiz, User student);

    // ✅ check if student passed any attempt for a quiz
    boolean existsByQuizAndStudentAndPassedTrue(Quiz quiz, User student);
}
