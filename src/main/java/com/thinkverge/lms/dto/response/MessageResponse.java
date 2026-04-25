package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {

    private Long id;
    private Long senderId;
    private String senderName;
    private String content;
    private Boolean aiMessage;
    private LocalDateTime sentAt;

    // ── Nested DTOs used by message endpoints ─────────────────────────────────

    @Data
    @Builder
    public static class UserSummary {
        private Long id;
        private String name;
        private String profileImage;
    }

    @Data
    @Builder
    public static class CourseChat {
        private Long courseId;
        private String courseTitle;
        private Long instructorId;
        private String instructorName;
        private String instructorProfileImage;
    }
}
