package com.thinkverge.lms.service;

import com.thinkverge.lms.dto.response.ProgressResponse;
import com.thinkverge.lms.model.*;
import com.thinkverge.lms.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final LessonRepository lessonRepository;
    private final LessonProgressRepository progressRepository;
    private final UserRepository userRepository;

    // VIDEO PROGRESS
    public void updateVideoProgress(
            Long lessonId,
            Integer percentage,
            String email
    ) {

        User student = userRepository
                .findByEmail(email)
                .orElseThrow();

        Lesson lesson = lessonRepository
                .findById(lessonId)
                .orElseThrow();

        LessonProgress progress = progressRepository
                .findByStudentAndLesson(student, lesson)
                .orElse(LessonProgress.builder()
                        .student(student)
                        .lesson(lesson)
                        .completed(false)
                        .build());

        progress.setCompletionPercentage(percentage);

        if (percentage >= 80) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }

        progressRepository.save(progress);
    }

    // TEXT COMPLETE
    public void completeTextLesson(
            Long lessonId,
            String email
    ) {

        User student = userRepository
                .findByEmail(email)
                .orElseThrow();

        Lesson lesson = lessonRepository
                .findById(lessonId)
                .orElseThrow();

        LessonProgress progress = progressRepository
                .findByStudentAndLesson(student, lesson)
                .orElse(LessonProgress.builder()
                        .student(student)
                        .lesson(lesson)
                        .build());

        progress.setCompleted(true);
        progress.setCompletionPercentage(100);
        progress.setCompletedAt(LocalDateTime.now());

        progressRepository.save(progress);
    }

    // GET PROGRESS (DTO)
    public List<ProgressResponse> getStudentProgress(String email) {

        User student = userRepository
                .findByEmail(email)
                .orElseThrow();

        return progressRepository
                .findByStudent(student)
                .stream()
                .map(p -> ProgressResponse.builder()
                        .lessonId(p.getLesson().getId())
                        .completed(p.getCompleted())
                        .percentage(p.getCompletionPercentage())
                        .build()
                )
                .collect(Collectors.toList());
    }
}