package com.thinkverge.lms.dto.request;

import com.thinkverge.lms.enums.LessonType;
import lombok.Data;

@Data
public class LessonRequest {
    private Long moduleId;
    private String title;
    private LessonType type;
    private String content;
    private String fileUrl;
    private Integer durationSeconds;
    private Integer orderIndex;
}