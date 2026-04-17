package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AssignmentResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Integer maxPoints;    // ✅ matches frontend field name
    private String pdfUrl;        // ✅ instructor's assignment PDF
}