package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.Course;
import com.thinkverge.lms.model.Message;
import com.thinkverge.lms.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // All messages in a course, ordered by time
    List<Message> findByCourseOrderBySentAtAsc(Course course);

    // Legacy — keep for compatibility
    List<Message> findBySenderAndReceiver(User sender, User receiver);

    // Direct human messages only (student ↔ instructor) — AI messages excluded
    @Query("""
        SELECT m FROM Message m
        WHERE m.course = :course
          AND m.isAiMessage = false
          AND (
            (m.sender = :userA AND m.receiver = :userB)
            OR (m.sender = :userB AND m.receiver = :userA)
          )
        ORDER BY m.sentAt ASC
        """)
    List<Message> findDirectMessagesBetweenUsersInCourse(
            @Param("userA") User userA,
            @Param("userB") User userB,
            @Param("course") Course course
    );

    // AI chat messages for a specific student in a course (student's AI tab only)
    @Query("""
        SELECT m FROM Message m
        WHERE m.course = :course
          AND m.isAiMessage = true
          AND m.receiver = :student
        ORDER BY m.sentAt ASC
        """)
    List<Message> findAiMessagesByStudentAndCourse(
            @Param("student") User student,
            @Param("course") Course course
    );
}