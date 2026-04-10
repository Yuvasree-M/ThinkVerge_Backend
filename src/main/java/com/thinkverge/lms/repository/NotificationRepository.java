package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.Notification;
import com.thinkverge.lms.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndIsRead(User user, Boolean isRead);
}