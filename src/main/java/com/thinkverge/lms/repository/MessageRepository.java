package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.Message;
import com.thinkverge.lms.model.User;
import com.thinkverge.lms.model.Course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByCourseOrderBySentAtAsc(Course course);

    List<Message> findBySenderAndReceiver(User sender, User receiver);
}