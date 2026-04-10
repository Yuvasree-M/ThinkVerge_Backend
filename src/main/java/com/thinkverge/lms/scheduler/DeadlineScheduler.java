package com.thinkverge.lms.scheduler;

import com.thinkverge.lms.model.Assignment;
import com.thinkverge.lms.model.Enrollment;
import com.thinkverge.lms.repository.AssignmentRepository;
import com.thinkverge.lms.repository.EnrollmentRepository;
import com.thinkverge.lms.repository.SubmissionRepository;
import com.thinkverge.lms.service.EmailService;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeadlineScheduler {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EmailService emailService;

    @Scheduled(fixedRate = 60000)
    public void sendDeadlineReminders() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime inTwoHours = now.plusHours(2);

        List<Assignment> assignments = assignmentRepository.findAll();

        for (Assignment assignment : assignments) {

            if (Boolean.TRUE.equals(assignment.getReminderSent())) continue;

            if (assignment.getDueDate() == null) continue;

            if (assignment.getDueDate().isAfter(now)
                    && assignment.getDueDate().isBefore(inTwoHours)) {

                List<Enrollment> enrollments =
                        enrollmentRepository.findByCourse(assignment.getCourse());

                for (Enrollment enrollment : enrollments) {

                    boolean submitted =
                            submissionRepository
                                    .existsByAssignmentAndStudent(
                                            assignment,
                                            enrollment.getStudent()
                                    );

                    if (!submitted) {

                        emailService.send(
                                enrollment.getStudent().getEmail(),
                                "Assignment Deadline Reminder",
                                "Assignment '" + assignment.getTitle()
                                        + "' is due in 2 hours."
                        );
                    }
                }

                assignment.setReminderSent(true);
                assignmentRepository.save(assignment);
            }
        }
    }
    
    
}