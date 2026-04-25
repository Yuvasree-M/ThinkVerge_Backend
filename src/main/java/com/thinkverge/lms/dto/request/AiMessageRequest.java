package com.thinkverge.lms.dto.request;

import lombok.Data;

@Data
public class AiMessageRequest {
    private Long courseId;
    private String content;   // the AI-generated reply text
}
