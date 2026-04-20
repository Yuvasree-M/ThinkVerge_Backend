package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestimonialResponse {
    private Long   id;
    private String studentName;
    private String profileImage;
    private String courseTitle;
    private String gradeLabel;
    private Integer averageScore;
    private String issuedAt;   // formatted date string
}
