package com.thinkverge.lms.dto.response;

import com.thinkverge.lms.enums.LessonType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class LessonResponse {
    private Long id;
    private String title;
    private LessonType type;
    private String content;
    private String fileUrl;
    private Integer durationSeconds;
    private Integer orderIndex;
    private LocalDateTime createdAt;
}