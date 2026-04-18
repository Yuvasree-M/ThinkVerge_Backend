//package com.thinkverge.lms.dto.response;
//
//import lombok.Builder;
//import lombok.Data;
//import java.time.LocalDateTime;
//
//@Data
//@Builder
//public class SubmissionResponse {
//    private Long id;
//    private Long assignmentId;
//    private String assignmentTitle;   // ✅ for student view
//    private String studentName;
//    private String fileUrl;
//    private Integer grade;            // ✅ renamed from marks (matches frontend)
//    private String feedback;
//    private String status;
//    private LocalDateTime submittedAt;
//}

package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class SubmissionResponse {
    private Long id;
    private Long assignmentId;
    private String assignmentTitle;   // for student view
    private Long courseId;            // for grouping submissions by course
    private String courseTitle;       // for display
    private String studentName;
    private String fileUrl;
    private String content;           // text content of submission
    private Integer grade;            // renamed from marks (matches frontend)
    private String feedback;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
}