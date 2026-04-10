package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProgressResponse {

    private Long lessonId;
    private Boolean completed;
    private Integer percentage;

}