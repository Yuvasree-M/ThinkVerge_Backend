package com.thinkverge.lms.dto.request;

import lombok.Data;

@Data
public class AiRequest {
    private Long courseId;
    private String content;
}