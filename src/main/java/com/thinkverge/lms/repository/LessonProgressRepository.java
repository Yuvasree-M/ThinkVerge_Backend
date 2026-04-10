package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.LessonProgress;
import com.thinkverge.lms.model.User;
import com.thinkverge.lms.model.Lesson;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository 
        extends JpaRepository<LessonProgress, Long> {

    List<LessonProgress> findByStudent(User student);

    Optional<LessonProgress> 
    findByStudentAndLesson(User student, Lesson lesson);

    List<LessonProgress> 
    findByStudentAndCompleted(User student, Boolean completed);

    // IMPORTANT for course progress
    List<LessonProgress> 
    findByStudentAndLessonModuleCourseId(
            User student,
            Long courseId
    );
}