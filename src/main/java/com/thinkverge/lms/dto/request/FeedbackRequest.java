package com.thinkverge.lms.dto.request;

import lombok.Data;

@Data
public class FeedbackRequest {
    private String name;
    private String message;
    private Integer rating;
    private String courseTitle;
}