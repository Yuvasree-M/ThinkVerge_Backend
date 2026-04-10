package com.thinkverge.lms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.thinkverge.lms.enums.SubmissionStatus;

@Entity
@Table(name = "submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    private Integer marks;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;
}