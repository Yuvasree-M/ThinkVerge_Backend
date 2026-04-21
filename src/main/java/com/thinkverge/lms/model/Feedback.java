package com.thinkverge.lms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private Integer rating;         // 1–5 stars

    private String courseTitle;     // which course the feedback is about

    @Builder.Default
    private boolean approved = false;   // admin must approve before it's public

    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();
}