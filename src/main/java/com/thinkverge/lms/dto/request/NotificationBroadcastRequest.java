package com.thinkverge.lms.dto.request;

import lombok.Data;

@Data
public class NotificationBroadcastRequest {

    private String title;
    private String message;
    private String targetRole; // ALL / STUDENT / INSTRUCTOR

}