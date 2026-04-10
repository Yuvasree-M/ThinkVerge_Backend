package com.thinkverge.lms.dto.request;

import lombok.Data;

@Data
public class QuizQuestionRequest {

    private Long quizId;
    private String question;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    private String correctOption;

}