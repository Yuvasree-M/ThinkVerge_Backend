package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SubmissionResponse {

    private Long id;
    private Long assignmentId;
    private String studentName;
    private String fileUrl;
    private Integer marks;
    private String feedback;
    private String status;
    private LocalDateTime submittedAt;

}