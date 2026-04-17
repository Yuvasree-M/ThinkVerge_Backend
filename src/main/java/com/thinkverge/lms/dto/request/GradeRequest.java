package com.thinkverge.lms.dto.request;

import lombok.Data;

@Data
public class GradeRequest {
    private Integer grade;     // ✅ renamed from marks
    private String feedback;
}