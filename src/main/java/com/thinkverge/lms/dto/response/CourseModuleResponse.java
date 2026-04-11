package com.thinkverge.lms.dto.response;

import lombok.*;

@Data
@Builder
public class CourseModuleResponse {
    private Long id;
    private String title;
    private String description;
    private Integer orderIndex;
}