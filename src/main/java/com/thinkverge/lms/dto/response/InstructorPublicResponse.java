package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InstructorPublicResponse {
    private Long   id;
    private String name;
    private String email;
    private String profileImage;
    private int    courseCount;
    private int    studentCount;  // total enrolled students across all their courses
    private String specialty;     // derived from their most common course category
}
