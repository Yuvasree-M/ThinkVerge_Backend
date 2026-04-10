package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseResponse {

    private Long id;
    private String title;
    private String description;
    private String thumbnail;

    private String instructorName;

    private String category;
    private Integer durationHours;

    private String status;

}