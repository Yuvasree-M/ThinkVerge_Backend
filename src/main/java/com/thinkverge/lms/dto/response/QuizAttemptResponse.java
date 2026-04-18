package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class QuizAttemptResponse {
    private Long id;
    private Long quizId;
    private String quizTitle;
    private Long moduleId;
    private Integer score;       // percentage
    private Integer passingScore;
    private Boolean passed;
    private LocalDateTime attemptedAt;
    private Integer totalQuestions;
    private Integer correctAnswers;
}
