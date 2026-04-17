package com.thinkverge.lms.service;

import com.thinkverge.lms.dto.request.LessonRequest;
import com.thinkverge.lms.dto.request.LessonUpdateRequest;
import com.thinkverge.lms.dto.response.LessonResponse;
import com.thinkverge.lms.enums.EnrollmentStatus;
import com.thinkverge.lms.enums.LessonType;
import com.thinkverge.lms.model.CourseModule;
import com.thinkverge.lms.model.Enrollment;
import com.thinkverge.lms.model.Lesson;
import com.thinkverge.lms.repository.CourseModuleRepository;
import com.thinkverge.lms.repository.EnrollmentRepository;
import com.thinkverge.lms.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseModuleRepository moduleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EmailService emailService;

    // ── CREATE ────────────────────────────────────────────
    public LessonResponse create(LessonRequest request) {
        CourseModule module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new RuntimeException("Module not found"));

        Lesson lesson = Lesson.builder()
                .module(module)
                .title(request.getTitle())
                .type(request.getType())
                .content(
                    request.getType() == LessonType.TEXT ? request.getContent() : null
                )
                .fileUrl(
                    request.getType() != LessonType.TEXT ? request.getFileUrl() : null
                )
                .durationSeconds(request.getDurationSeconds())
                .orderIndex(request.getOrderIndex())
                .createdAt(LocalDateTime.now())
                .build();

        Lesson saved = lessonRepository.save(lesson);

        // notify enrolled students
        List<Enrollment> enrollments = enrollmentRepository.findByCourse(module.getCourse());
        for (Enrollment e : enrollments) {
            if (e.getStatus() == EnrollmentStatus.APPROVED) {
                emailService.sendNewLesson(
                        e.getStudent().getEmail(),
                        saved.getTitle(),
                        module.getCourse().getTitle()
                );
            }
        }

        return toResponse(saved);
    }

    // ── GET BY MODULE ─────────────────────────────────────
    public List<LessonResponse> getByModule(Long moduleId) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        return lessonRepository
                .findByModuleOrderByOrderIndexAsc(module)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── UPDATE ────────────────────────────────────────────
    public LessonResponse update(Long id, LessonUpdateRequest req) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        lesson.setTitle(req.getTitle());
        lesson.setType(req.getType());

        // ✅ TEXT only
        lesson.setContent(req.getType().name().equals("TEXT") ? req.getContent() : null);

        // ✅ FILES
        lesson.setFileUrl(
                req.getType().name().equals("VIDEO") ||
                req.getType().name().equals("PDF") ||
                req.getType().name().equals("IMAGE")
                        ? req.getFileUrl()
                        : null
        );

        lesson.setDurationSeconds(req.getDurationSeconds());
        lesson.setOrderIndex(req.getOrderIndex());
        return toResponse(lessonRepository.save(lesson));
    }

    // ── DELETE ────────────────────────────────────────────
    public void delete(Long id) {
        lessonRepository.deleteById(id);
    }

    // ── MAPPER ────────────────────────────────────────────
    private LessonResponse toResponse(Lesson l) {
        return LessonResponse.builder()
                .id(l.getId())
                .title(l.getTitle())
                .type(l.getType())
                .content(l.getContent())
                .fileUrl(l.getFileUrl())
                .durationSeconds(l.getDurationSeconds())
                .orderIndex(l.getOrderIndex())
                .createdAt(l.getCreatedAt())
                .build();
    }
}