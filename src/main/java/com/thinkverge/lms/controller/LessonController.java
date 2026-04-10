package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.request.LessonRequest;
import com.thinkverge.lms.model.Lesson;
import com.thinkverge.lms.service.LessonService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping
    public Lesson create(
            @RequestPart("data") LessonRequest request,
            @RequestPart(value = "video", required = false) MultipartFile video
    ) {
        return lessonService.createLesson(request, video);
    }

    @GetMapping("/module/{moduleId}")
    public List<Lesson> getByModule(
            @PathVariable Long moduleId
    ) {
        return lessonService.getByModule(moduleId);
    }
}