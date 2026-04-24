package com.thinkverge.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardResponse {

    private List<EnrollmentResponse> enrollments;
    private List<ProgressResponse>   progress;
    private List<SubmissionResponse> submissions;
}