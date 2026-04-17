package com.thinkverge.lms.controller;

import com.thinkverge.lms.service.FileUploadService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService uploadService;

    @PostMapping
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) {
        String url = uploadService.uploadFile(file);
        return Map.of("url", url);
    }
}