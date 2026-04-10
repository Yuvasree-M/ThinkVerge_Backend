package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.response.ProgressResponse;
import com.thinkverge.lms.service.ProgressService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping("/video/{lessonId}")
    public void updateVideo(
            @PathVariable Long lessonId,
            @RequestParam Integer percentage,
            Authentication auth
    ) {
        progressService.updateVideoProgress(
                lessonId,
                percentage,
                auth.getName()
        );
    }

    @PostMapping("/text/{lessonId}/complete")
    public void completeText(
            @PathVariable Long lessonId,
            Authentication auth
    ) {
        progressService.completeTextLesson(
                lessonId,
                auth.getName()
        );
    }

    @GetMapping("/my")
    public List<ProgressResponse> myProgress(
            Authentication auth
    ) {
        return progressService.getStudentProgress(
                auth.getName()
        );
    }
}