package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProgressResponse {
    private Long lessonId;
    private String lessonTitle;   // ← NEW: lesson title for UI display
    private String lessonType;    // ← NEW: VIDEO / TEXT / PDF / IMAGE
    private Long courseId;        // ← NEW: for frontend grouping by course
    private Boolean completed;
    private Integer percentage;
}