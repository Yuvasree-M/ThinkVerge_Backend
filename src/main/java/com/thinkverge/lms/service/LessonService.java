package com.thinkverge.lms.service;

import com.thinkverge.lms.dto.request.LessonRequest;
import com.thinkverge.lms.enums.EnrollmentStatus;
import com.thinkverge.lms.model.CourseModule;
import com.thinkverge.lms.model.Enrollment;
import com.thinkverge.lms.model.Lesson;
import com.thinkverge.lms.repository.CourseModuleRepository;
import com.thinkverge.lms.repository.LessonRepository;
import com.thinkverge.lms.repository.EnrollmentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseModuleRepository moduleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FileUploadService fileUploadService;
    private final EmailService emailService;
    
    public Lesson createLesson(
            LessonRequest request,
            MultipartFile videoFile
    ) {

        CourseModule module = moduleRepository
                .findById(request.getModuleId())
                .orElseThrow();

        String videoUrl = request.getVideoUrl();

        // upload video if file provided
        if (videoFile != null && !videoFile.isEmpty()) {
            videoUrl = fileUploadService.uploadFile(videoFile);
        }

        Lesson lesson = Lesson.builder()
                .module(module)
                .title(request.getTitle())
                .type(request.getType())
                .content(request.getContent())
                .videoUrl(videoUrl)
                .durationSeconds(request.getDurationSeconds())
                .orderIndex(request.getOrderIndex())
                .createdAt(LocalDateTime.now())
                .build();

        Lesson savedLesson = lessonRepository.save(lesson);

        // Send email to all approved enrolled students

        List<Enrollment> enrollments = enrollmentRepository.findByCourse(module.getCourse());
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStatus() == EnrollmentStatus.APPROVED) {
                emailService.sendNewLesson(
                        enrollment.getStudent().getEmail(),
                        savedLesson.getTitle(),
                        module.getCourse().getTitle()
                );
            }
        }

        return savedLesson;
    }

    public List<Lesson> getByModule(Long moduleId) {

        CourseModule module = moduleRepository
                .findById(moduleId)
                .orElseThrow();

        return lessonRepository
                .findByModuleOrderByOrderIndexAsc(module);
    }
}