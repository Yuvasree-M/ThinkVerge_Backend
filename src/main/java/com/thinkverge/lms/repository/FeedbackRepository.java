package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

  
    List<Feedback> findByApprovedFalseOrderBySubmittedAtDesc();

    List<Feedback> findByApprovedTrueOrderBySubmittedAtDesc();
}