package com.thinkverge.lms.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AssignmentRequest {
    private Long courseId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Integer maxMarks;
    private String pdfUrl;        // ✅ ADD THIS
}