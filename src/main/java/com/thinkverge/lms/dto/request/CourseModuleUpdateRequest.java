package com.thinkverge.lms.dto.request;

import lombok.Data;

@Data
public class CourseModuleUpdateRequest {
    private String title;
    private String description;
    private Integer orderIndex;
}