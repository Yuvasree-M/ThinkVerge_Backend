package com.thinkverge.lms.dto.request;

import lombok.Data;

@Data
public class ProgressUpdateRequest {

    private Long lessonId;
    private Integer percentage;

}