package com.thinkverge.lms.dto.request;

import lombok.Data;

@Data
public class CourseRequest {

    private String title;
    private String description;
    private String thumbnail;
    private String category;
    private Integer durationHours;

}