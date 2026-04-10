package com.thinkverge.lms.controller;

import com.thinkverge.lms.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService uploadService;

    @PostMapping
    public String upload(
            @RequestParam("file") MultipartFile file
    ) {
        return uploadService.uploadFile(file);
    }
}