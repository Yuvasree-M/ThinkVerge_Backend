package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentResponse {

    private Long id;

    private CourseDto course;
    private StudentDto student;

    private String status;
    private LocalDateTime requestedAt;


    @Data
    @Builder
    public static class CourseDto {
        private Long id;
        private String title;
    }


    @Data
    @Builder
    public static class StudentDto {
        private String name;
        private String email;
    }
}