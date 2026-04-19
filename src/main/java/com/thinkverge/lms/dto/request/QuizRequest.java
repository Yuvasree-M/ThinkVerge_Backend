//package com.thinkverge.lms.dto.request;
//
//import lombok.Data;
//import java.util.List;
//
//@Data
//public class QuizRequest {
//    private Long moduleId;       // ✅ one quiz per module
//    private Long courseId;       // kept for backward compat
//    private String title;
//    private Integer passingScore; // percentage e.g. 70
//    private List<QuizQuestionRequest> questions; // ✅ create questions inline
//}

package com.thinkverge.lms.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class QuizRequest {
    private Long moduleId;        // null when creating a final quiz
    private Long courseId;        // required for final quiz
    private String title;
    private Integer passingScore; // percentage e.g. 70
    private Boolean isFinalQuiz;  // true = course final exam
    private List<QuizQuestionRequest> questions;
}
