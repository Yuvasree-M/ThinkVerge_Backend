package com.thinkverge.lms.dto.request;

import lombok.Data;

@Data
public class MessageRequest {

    private Long receiverId;
    private Long courseId;
    private String content;

}