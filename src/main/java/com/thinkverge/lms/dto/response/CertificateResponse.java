package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CertificateResponse {
    private Long id;
    private String courseTitle;
    private String studentName;
    private String instructorName;  // for signature on certificate
    private String certificateUrl;
    private LocalDateTime issuedAt;
    private Integer averageScore;
    private String gradeLabel;
}
