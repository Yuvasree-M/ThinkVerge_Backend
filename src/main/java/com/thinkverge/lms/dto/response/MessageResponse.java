package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {

    private Long id;
    private String senderName;
    private String content;
    private Boolean aiMessage;
    private LocalDateTime sentAt;

}