package com.thinkverge.lms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.thinkverge.lms.enums.NotificationType;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}