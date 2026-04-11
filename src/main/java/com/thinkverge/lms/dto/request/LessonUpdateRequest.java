package com.thinkverge.lms.dto.request;

import com.thinkverge.lms.enums.LessonType;
import lombok.Data;

@Data
public class LessonUpdateRequest {
    private String title;
    private LessonType type;
    private String content;
    private String videoUrl;
    private Integer durationSeconds;
    private Integer orderIndex;
}