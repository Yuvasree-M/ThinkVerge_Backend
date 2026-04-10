package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.Lesson;
import com.thinkverge.lms.model.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByModuleOrderByOrderIndexAsc(CourseModule module);
}