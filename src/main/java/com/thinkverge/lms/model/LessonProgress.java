package com.thinkverge.lms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    private Boolean completed;

    @Column(name = "completion_percentage")
    private Integer completionPercentage;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}