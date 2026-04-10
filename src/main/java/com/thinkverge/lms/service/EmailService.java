package com.thinkverge.lms.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // =============================
    // GENERIC SEND
    // =============================
    public void send(String to, String subject, String body) {
        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================
    // DEADLINE REMINDER
    // =============================
    public void sendDeadlineReminder(
            String to,
            String studentName,
            String assignment,
            String time
    ) {

        String body = """
                <div style="font-family:Arial;padding:20px">
                    <h2>Assignment Deadline Reminder</h2>

                    <p>Hello %s,</p>

                    <p>Your assignment <b>%s</b> is due in %s.</p>

                    <p>Please submit before deadline.</p>

                    <br>
                    <small>ThinkVerge LMS</small>
                </div>
                """.formatted(studentName, assignment, time);

        send(to, "Assignment Deadline Reminder", body);
    }

    // =============================
    // ASSIGNMENT GRADED
    // =============================
    public void sendAssignmentGraded(
            String to,
            String assignment,
            Integer marks,
            String feedback
    ) {

        String body = """
                <div style="font-family:Arial;padding:20px">
                    <h2>Assignment Graded</h2>

                    <p>Your assignment <b>%s</b> has been graded.</p>

                    <p><b>Marks:</b> %d</p>
                    <p><b>Feedback:</b> %s</p>

                    <br>
                    <small>ThinkVerge LMS</small>
                </div>
                """.formatted(assignment, marks, feedback);

        send(to, "Assignment Graded", body);
    }

    // =============================
    // ASSIGNMENT SUBMITTED
    // =============================
    public void sendAssignmentSubmitted(
            String to,
            String student,
            String assignment
    ) {

        String body = """
                <div style="font-family:Arial;padding:20px">
                    <h2>Assignment Submitted</h2>

                    <p>Student <b>%s</b> submitted assignment <b>%s</b></p>

                    <br>
                    <small>ThinkVerge LMS</small>
                </div>
                """.formatted(student, assignment);

        send(to, "Assignment Submitted", body);
    }

    // =============================
    // ENROLLMENT APPROVED
    // =============================
    public void sendEnrollmentApproved(
            String to,
            String course
    ) {

        String body = """
                <div style="font-family:Arial;padding:20px">
                    <h2>Enrollment Approved</h2>

                    <p>You are now enrolled in <b>%s</b></p>

                    <p>You can now access course content.</p>

                    <br>
                    <small>ThinkVerge LMS</small>
                </div>
                """.formatted(course);

        send(to, "Enrollment Approved", body);
    }

    // =============================
    // ENROLLMENT REQUESTED
    // =============================
    public void sendEnrollmentRequested(
            String to,
            String student,
            String course
    ) {

        String body = """
                <div style="font-family:Arial;padding:20px">
                    <h2>Enrollment Request</h2>

                    <p><b>%s</b> requested enrollment for course <b>%s</b></p>

                    <br>
                    <small>ThinkVerge LMS</small>
                </div>
                """.formatted(student, course);

        send(to, "Enrollment Request", body);
    }

 // =============================
 // NEW COURSE UPLOADED → Admin
 // =============================
 public void sendNewCourse(
         String to,
         String course,
         String instructor
 ) {

     String body = """
             <div style="font-family:Arial;padding:20px">
                 <h2>New Course Submitted</h2>

                 <p>Instructor <b>%s</b> has submitted a new course <b>%s</b>.</p>

                 <p>Please review and approve or reject the course.</p>

                 <br>
                 <small>ThinkVerge LMS</small>
             </div>
             """.formatted(instructor, course);

     send(to, "New Course Submitted", body);
 }
 
    // =============================
    // COURSE APPROVED
    // =============================
    public void sendCourseApproved(
            String to,
            String course
    ) {

        String body = """
                <div style="font-family:Arial;padding:20px">
                    <h2>Course Approved</h2>

                    <p>Your course <b>%s</b> has been approved.</p>

                    <br>
                    <small>ThinkVerge LMS</small>
                </div>
                """.formatted(course);

        send(to, "Course Approved", body);
    }

    // =============================
    // COURSE REJECTED
    // =============================
    public void sendCourseRejected(
            String to,
            String course
    ) {

        String body = """
                <div style="font-family:Arial;padding:20px">
                    <h2>Course Rejected</h2>

                    <p>Your course <b>%s</b> has been rejected.</p>

                    <br>
                    <small>ThinkVerge LMS</small>
                </div>
                """.formatted(course);

        send(to, "Course Rejected", body);
    }

    // =============================
    // NEW ASSIGNMENT CREATED
    // =============================
    public void sendNewAssignment(
            String to,
            String assignment,
            String course
    ) {

        String body = """
                <div style="font-family:Arial;padding:20px">
                    <h2>New Assignment</h2>

                    <p>New assignment <b>%s</b> added to course <b>%s</b></p>

                    <br>
                    <small>ThinkVerge LMS</small>
                </div>
                """.formatted(assignment, course);

        send(to, "New Assignment Created", body);
    }
    
 // =============================
 // NEW LESSON UPLOADED
 // =============================
 public void sendNewLesson(
         String to,
         String lesson,
         String course
 ) {

     String body = """
             <div style="font-family:Arial;padding:20px">
                 <h2>New Lesson Available</h2>

                 <p>New lesson <b>%s</b> added to course <b>%s</b></p>

                 <p>You can now watch the lesson.</p>

                 <br>
                 <small>ThinkVerge LMS</small>
             </div>
             """.formatted(lesson, course);

     send(to, "New Lesson Uploaded", body);
 }
}