package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackResponse {
    private Long id;
    private String name;
    private String message;
    private Integer rating;
    private String courseTitle;
    private boolean approved;
    private LocalDateTime submittedAt;
}