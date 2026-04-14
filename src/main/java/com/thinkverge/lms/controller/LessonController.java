package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.request.LessonRequest;
import com.thinkverge.lms.dto.request.LessonUpdateRequest;
import com.thinkverge.lms.dto.response.LessonResponse;
import com.thinkverge.lms.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    
    @PostMapping
    public LessonResponse create(@RequestBody LessonRequest request) {
        return lessonService.create(request);
    }

    @GetMapping("/module/{moduleId}")
    public List<LessonResponse> getByModule(@PathVariable Long moduleId) {
        return lessonService.getByModule(moduleId);
    }

    @PutMapping("/{id}")
    public LessonResponse update(@PathVariable Long id,
                                  @RequestBody LessonUpdateRequest req) {
        return lessonService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        lessonService.delete(id);
    }
}