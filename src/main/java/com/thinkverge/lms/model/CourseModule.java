package com.thinkverge.lms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "modules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}