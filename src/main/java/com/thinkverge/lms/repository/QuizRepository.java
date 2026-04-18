package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.Quiz;
import com.thinkverge.lms.model.Course;
import com.thinkverge.lms.model.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByCourse(Course course);

    // ✅ one quiz per module
    Optional<Quiz> findByModule(CourseModule module);

    List<Quiz> findByCourseId(Long courseId);
}
