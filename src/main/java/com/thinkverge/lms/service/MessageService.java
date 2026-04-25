package com.thinkverge.lms.service;

import com.thinkverge.lms.dto.request.AiMessageRequest;
import com.thinkverge.lms.dto.request.MessageRequest;
import com.thinkverge.lms.dto.response.MessageResponse;
import com.thinkverge.lms.model.Course;
import com.thinkverge.lms.model.Enrollment;
import com.thinkverge.lms.model.Message;
import com.thinkverge.lms.model.User;
import com.thinkverge.lms.enums.EnrollmentStatus;
import com.thinkverge.lms.repository.CourseRepository;
import com.thinkverge.lms.repository.EnrollmentRepository;
import com.thinkverge.lms.repository.MessageRepository;
import com.thinkverge.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AiService aiService;

    // ─── Send a human message (student ↔ instructor) ─────────────────────────

    public MessageResponse sendMessage(MessageRequest request, String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        validateMessagePermission(sender, receiver, course);

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .course(course)
                .content(request.getContent())
                .isAiMessage(false)
                .sentAt(LocalDateTime.now())
                .build();

        messageRepository.save(message);
        return toResponse(message);
    }

    // ─── Persist an AI message log entry (no AI call) ────────────────────────
    // Used by the frontend to save AI conversation turns to the DB.
    // sender = the student, receiver = null (AI has no User record),
    // isAiMessage = true so the frontend knows how to render it.

    public MessageResponse saveAiMessage(AiMessageRequest request, String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Only enrolled students (or the course instructor) may log AI messages
        validateReadPermission(sender, course);

        Message message = Message.builder()
                .sender(sender)
                .receiver(null)          // AI has no user record
                .course(course)
                .content(request.getContent())
                .isAiMessage(true)
                .sentAt(LocalDateTime.now())
                .build();

        messageRepository.save(message);
        return toResponse(message);
    }

    // ─── Ask AI and return the reply (Gemini via Spring AI) ──────────────────
    // Frontend can call POST /api/messages/ai/ask instead of calling Gemini
    // directly.  This method:
    //   1. Validates the caller is enrolled/instructor
    //   2. Calls AiService (Spring AI → Gemini)
    //   3. Persists both the question and the reply as AI messages
    //   4. Returns the AI reply text

    public String askAi(AiMessageRequest request, String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        validateReadPermission(sender, course);

        // Persist the student question
        Message questionMsg = Message.builder()
                .sender(sender)
                .receiver(null)
                .course(course)
                .content(request.getContent())
                .isAiMessage(false)
                .sentAt(LocalDateTime.now())
                .build();
        messageRepository.save(questionMsg);

        // Call Gemini via Spring AI
        String aiReply = aiService.getAiReply(course.getTitle(), request.getContent());

        // Persist the AI reply
        Message replyMsg = Message.builder()
                .sender(null)            // AI has no sender User
                .receiver(sender)
                .course(course)
                .content(aiReply)
                .isAiMessage(true)
                .sentAt(LocalDateTime.now())
                .build();
        messageRepository.save(replyMsg);

        return aiReply;
    }

    // ─── Get course chat history (all messages for a course) ─────────────────

    public List<MessageResponse> getCourseMessages(Long courseId, String requestingUserEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        User requestingUser = userRepository.findByEmail(requestingUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateReadPermission(requestingUser, course);

        return messageRepository.findByCourseOrderBySentAtAsc(course)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Get direct messages between two users in a course ───────────────────

    public List<MessageResponse> getDirectMessages(Long courseId, Long otherUserId, String requestingUserEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        User requestingUser = userRepository.findByEmail(requestingUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("Other user not found"));

        List<Message> messages = messageRepository.findDirectMessagesBetweenUsersInCourse(
                requestingUser, otherUser, course);

        return messages.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ─── Get list of students the instructor can chat with (for a course) ────

    public List<MessageResponse.UserSummary> getEnrolledStudentsForInstructor(Long courseId, String instructorEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new RuntimeException("You are not the instructor of this course");
        }

        return enrollmentRepository.findByCourse(course).stream()
                .map(Enrollment::getStudent)
                .map(s -> MessageResponse.UserSummary.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .profileImage(s.getProfileImage())
                        .build())
                .collect(Collectors.toList());
    }

    // ─── Get instructor info for student (per course) ─────────────────────────

    public MessageResponse.UserSummary getInstructorForCourse(Long courseId, String studentEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        boolean enrolled = enrollmentRepository.existsByStudentAndCourse(student, course);
        if (!enrolled) {
            throw new RuntimeException("You are not enrolled in this course");
        }

        User instructor = course.getInstructor();
        return MessageResponse.UserSummary.builder()
                .id(instructor.getId())
                .name(instructor.getName())
                .profileImage(instructor.getProfileImage())
                .build();
    }

    // ─── Get courses with messaging available (for sidebar list) ─────────────

    public List<MessageResponse.CourseChat> getAvailableChatsForStudent(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return enrollmentRepository.findByStudentAndStatus(student, EnrollmentStatus.APPROVED)
                .stream()
                .map(e -> {
                    Course c = e.getCourse();
                    User instr = c.getInstructor();
                    return MessageResponse.CourseChat.builder()
                            .courseId(c.getId())
                            .courseTitle(c.getTitle())
                            .instructorId(instr.getId())
                            .instructorName(instr.getName())
                            .instructorProfileImage(instr.getProfileImage())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<MessageResponse.CourseChat> getAvailableChatsForInstructor(String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        return courseRepository.findByInstructor(instructor).stream()
                .map(c -> MessageResponse.CourseChat.builder()
                        .courseId(c.getId())
                        .courseTitle(c.getTitle())
                        .instructorId(instructor.getId())
                        .instructorName(instructor.getName())
                        .instructorProfileImage(instructor.getProfileImage())
                        .build())
                .collect(Collectors.toList());
    }

    // ─── Validation helpers ───────────────────────────────────────────────────

    private void validateMessagePermission(User sender, User receiver, Course course) {
        String senderRole = sender.getRole().name();

        if (senderRole.equals("STUDENT")) {
            if (!course.getInstructor().getId().equals(receiver.getId())) {
                throw new RuntimeException("Students can only message the course instructor");
            }
            if (!enrollmentRepository.existsByStudentAndCourse(sender, course)) {
                throw new RuntimeException("You are not enrolled in this course");
            }
        } else if (senderRole.equals("INSTRUCTOR")) {
            if (!course.getInstructor().getId().equals(sender.getId())) {
                throw new RuntimeException("You are not the instructor of this course");
            }
            if (!enrollmentRepository.existsByStudentAndCourse(receiver, course)) {
                throw new RuntimeException("Student is not enrolled in this course");
            }
        } else {
            throw new RuntimeException("Only students and instructors can send messages");
        }
    }

    private void validateReadPermission(User user, Course course) {
        String role = user.getRole().name();
        if (role.equals("INSTRUCTOR")) {
            if (!course.getInstructor().getId().equals(user.getId())) {
                throw new RuntimeException("Access denied");
            }
        } else if (role.equals("STUDENT")) {
            if (!enrollmentRepository.existsByStudentAndCourse(user, course)) {
                throw new RuntimeException("You are not enrolled in this course");
            }
        } else {
            throw new RuntimeException("Access denied");
        }
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private MessageResponse toResponse(Message msg) {
        return MessageResponse.builder()
                .id(msg.getId())
                .senderName(msg.getSender() != null ? msg.getSender().getName() : "AI Assistant")
                .senderId(msg.getSender() != null ? msg.getSender().getId() : null)
                .content(msg.getContent())
                .aiMessage(msg.getIsAiMessage())
                .sentAt(msg.getSentAt())
                .build();
    }
}