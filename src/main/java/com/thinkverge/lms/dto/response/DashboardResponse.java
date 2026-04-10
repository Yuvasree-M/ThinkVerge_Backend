package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {

    private Long totalUsers;
    private Long totalStudents;
    private Long totalInstructors;
    private Long totalCourses;
    private Long pendingCourses;
    private Long activeEnrollments;

}