package com.thinkverge.lms.dto.request;

import lombok.Data;

@Data
public class CourseModuleRequest {

    private Long courseId;
    private String title;
    private String description;
    private Integer orderIndex;

}