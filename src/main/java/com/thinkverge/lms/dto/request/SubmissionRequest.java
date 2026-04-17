package com.thinkverge.lms.dto.request;

import lombok.Data;

@Data
public class SubmissionRequest {
    private Long assignmentId;
    private String fileUrl;     // Cloudinary PDF URL
    private String content;     // ✅ optional text answer
}