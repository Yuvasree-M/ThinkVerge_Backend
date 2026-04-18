package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.request.QuizRequest;
import com.thinkverge.lms.dto.request.QuizSubmitRequest;
import com.thinkverge.lms.dto.response.*;
import com.thinkverge.lms.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // ── INSTRUCTOR ────────────────────────────────────────

    // Create or replace quiz for a module
    @PostMapping
    public QuizResponse create(@RequestBody QuizRequest request) {
        return quizService.createForModule(request);
    }

    // Get quiz with correct answers (for instructor)
    @GetMapping("/module/{moduleId}/instructor")
    public Optional<QuizResponse> getForInstructor(@PathVariable Long moduleId) {
        return quizService.getByModuleForInstructor(moduleId);
    }

    // Delete a quiz
    @DeleteMapping("/{quizId}")
    public void delete(@PathVariable Long quizId) {
        quizService.deleteQuiz(quizId);
    }

    // ── STUDENT ───────────────────────────────────────────

    // Get quiz WITHOUT correct answers
    @GetMapping("/module/{moduleId}")
    public Optional<QuizResponse> getForStudent(@PathVariable Long moduleId) {
        return quizService.getByModuleForStudent(moduleId);
    }

    // Submit a quiz attempt
    @PostMapping("/submit")
    public QuizAttemptResponse submit(
            @RequestBody QuizSubmitRequest request,
            Authentication auth
    ) {
        return quizService.submitAttempt(request, auth.getName());
    }

    // Get my quiz attempts for a course
    @GetMapping("/my/course/{courseId}")
    public List<QuizAttemptResponse> myAttempts(
            @PathVariable Long courseId,
            Authentication auth
    ) {
        return quizService.myAttemptsForCourse(courseId, auth.getName());
    }

    // Get module unlock statuses for a course
    @GetMapping("/module-status/{courseId}")
    public List<ModuleStatusResponse> moduleStatuses(
            @PathVariable Long courseId,
            Authentication auth
    ) {
        return quizService.getModuleStatuses(courseId, auth.getName());
    }
}
