package com.thinkverge.lms.dto.request;

import lombok.Data;

@Data
public class QuizRequest {

    private Long courseId;
    private String title;
    private Integer passingScore;

}