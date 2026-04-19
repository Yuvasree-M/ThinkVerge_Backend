//package com.thinkverge.lms.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "quizzes")
//@Data @NoArgsConstructor @AllArgsConstructor @Builder
//public class Quiz {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // ✅ Linked to MODULE (one quiz per module), keep course for backward compat
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "module_id")
//    private CourseModule module;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "course_id")
//    private Course course;
//
//    @Column(nullable = false)
//    private String title;
//
//    @Column(name = "passing_score")
//    private Integer passingScore;   // e.g. 70 means 70%
//
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//}
package com.thinkverge.lms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quizzes")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Linked to MODULE (one quiz per module), keep course for backward compat
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private CourseModule module;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private String title;

    @Column(name = "passing_score")
    private Integer passingScore;   // e.g. 70 means 70%

    // true = course-level final exam (module is null), false = per-module quiz
    @Column(name = "is_final_quiz")
    private Boolean isFinalQuiz = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
