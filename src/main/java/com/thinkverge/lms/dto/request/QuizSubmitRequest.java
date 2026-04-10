package com.thinkverge.lms.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class QuizSubmitRequest {

    private Long quizId;

    // questionId -> selected option
    private Map<Long, String> answers;

}