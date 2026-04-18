package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class QuizResponse {
    private Long id;
    private Long moduleId;
    private Long courseId;
    private String title;
    private Integer passingScore;
    private List<QuizQuestionResponse> questions;
}
