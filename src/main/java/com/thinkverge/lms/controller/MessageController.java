//package com.thinkverge.lms.controller;
//
//import com.thinkverge.lms.dto.request.AiMessageRequest;
//import com.thinkverge.lms.dto.request.MessageRequest;
//import com.thinkverge.lms.dto.response.ApiResponse;
//import com.thinkverge.lms.dto.response.MessageResponse;
//import com.thinkverge.lms.service.MessageService;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/messages")
//@RequiredArgsConstructor
//public class MessageController {
//
//    private final MessageService messageService;
//
//    // ── Send a human message (student → instructor or instructor → student) ──
//    @PostMapping
//    public ResponseEntity<MessageResponse> sendMessage(
//            @RequestBody MessageRequest request,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        MessageResponse response = messageService.sendMessage(request, userDetails.getUsername());
//        return ResponseEntity.ok(response);
//    }
//
//    // ── Persist an AI chat message (log only, no Gemini call) ────────────────
//    @PostMapping("/ai/chat")
//    public ResponseEntity<MessageResponse> saveAiChatMessage(
//            @RequestBody AiMessageRequest request,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        MessageResponse response = messageService.saveAiMessage(request, userDetails.getUsername());
//        return ResponseEntity.ok(response);
//    }
//
//    // ── Ask AI: Spring AI calls Gemini, persists both turns, returns reply ───
//    // POST /api/messages/ai/ask  { courseId, content }
//    // Response: ApiResponse<String> { success, message, data: "<ai reply>" }
//    @PostMapping("/ai/ask")
//    public ResponseEntity<ApiResponse<String>> askAi(
//            @RequestBody AiMessageRequest request,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        String reply = messageService.askAi(request, userDetails.getUsername());
//        return ResponseEntity.ok(ApiResponse.ok("AI reply generated", reply));
//    }
//
//    // ── Get direct messages between current user and another user in a course ─
//    @GetMapping("/direct/{courseId}/{otherUserId}")
//    public ResponseEntity<List<MessageResponse>> getDirectMessages(
//            @PathVariable Long courseId,
//            @PathVariable Long otherUserId,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        List<MessageResponse> messages = messageService.getDirectMessages(
//                courseId, otherUserId, userDetails.getUsername());
//        return ResponseEntity.ok(messages);
//    }
//
//    // ── Get all messages in a course (instructor board view) ─────────────────
//    @GetMapping("/course/{courseId}")
//    public ResponseEntity<List<MessageResponse>> getCourseMessages(
//            @PathVariable Long courseId,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        List<MessageResponse> messages = messageService.getCourseMessages(
//                courseId, userDetails.getUsername());
//        return ResponseEntity.ok(messages);
//    }
//
//    // ── Get enrolled students for instructor (for chat sidebar) ──────────────
//    @GetMapping("/course/{courseId}/students")
//    public ResponseEntity<List<MessageResponse.UserSummary>> getStudentsForChat(
//            @PathVariable Long courseId,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        List<MessageResponse.UserSummary> students =
//                messageService.getEnrolledStudentsForInstructor(courseId, userDetails.getUsername());
//        return ResponseEntity.ok(students);
//    }
//
//    // ── Get instructor info for student (per course) ──────────────────────────
//    @GetMapping("/course/{courseId}/instructor")
//    public ResponseEntity<MessageResponse.UserSummary> getInstructorForCourse(
//            @PathVariable Long courseId,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        MessageResponse.UserSummary instructor =
//                messageService.getInstructorForCourse(courseId, userDetails.getUsername());
//        return ResponseEntity.ok(instructor);
//    }
//
//    // ── Get available chats list (for sidebar) ────────────────────────────────
//    @GetMapping("/chats/student")
//    public ResponseEntity<List<MessageResponse.CourseChat>> getStudentChats(
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        List<MessageResponse.CourseChat> chats =
//                messageService.getAvailableChatsForStudent(userDetails.getUsername());
//        return ResponseEntity.ok(chats);
//    }
//
//    @GetMapping("/chats/instructor")
//    public ResponseEntity<List<MessageResponse.CourseChat>> getInstructorChats(
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        List<MessageResponse.CourseChat> chats =
//                messageService.getAvailableChatsForInstructor(userDetails.getUsername());
//        return ResponseEntity.ok(chats);
//    }
//}


package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.request.AiMessageRequest;
import com.thinkverge.lms.dto.request.MessageRequest;
import com.thinkverge.lms.dto.response.ApiResponse;
import com.thinkverge.lms.dto.response.MessageResponse;
import com.thinkverge.lms.service.MessageService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // ── Send a human message (student → instructor or instructor → student) ──
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestBody MessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        MessageResponse response = messageService.sendMessage(request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // ── Ask AI: calls Groq/Llama, persists both turns, returns reply ─────────
    // POST /api/messages/ai/ask  { courseId, content }
    @PostMapping("/ai/ask")
    public ResponseEntity<ApiResponse<String>> askAi(
            @RequestBody AiMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String reply = messageService.askAi(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("AI reply generated", reply));
    }

    // ── Get AI chat history for the current student in a course ─────────────
    // GET /api/messages/ai/{courseId}
    @GetMapping("/ai/{courseId}")
    public ResponseEntity<List<MessageResponse>> getAiMessages(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<MessageResponse> messages = messageService.getAiMessages(courseId, userDetails.getUsername());
        return ResponseEntity.ok(messages);
    }

    // ── Clear all AI chat messages for current user in a course ─────────────
    // DELETE /api/messages/ai/clear/{courseId}
    @DeleteMapping("/ai/clear/{courseId}")
    public ResponseEntity<ApiResponse<Void>> clearAiChat(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        messageService.clearAiChat(courseId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("AI chat cleared", null));
    }

    // ── Get direct messages between current user and another user in a course ─
    @GetMapping("/direct/{courseId}/{otherUserId}")
    public ResponseEntity<List<MessageResponse>> getDirectMessages(
            @PathVariable Long courseId,
            @PathVariable Long otherUserId,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<MessageResponse> messages = messageService.getDirectMessages(
                courseId, otherUserId, userDetails.getUsername());
        return ResponseEntity.ok(messages);
    }

    // ── Clear all direct messages between current user and another user ───────
    // DELETE /api/messages/direct/clear/{courseId}/{otherUserId}
    @DeleteMapping("/direct/clear/{courseId}/{otherUserId}")
    public ResponseEntity<ApiResponse<Void>> clearDirectChat(
            @PathVariable Long courseId,
            @PathVariable Long otherUserId,
            @AuthenticationPrincipal UserDetails userDetails) {

        messageService.clearDirectChat(courseId, otherUserId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Chat cleared", null));
    }

    // ── Delete a single message by ID ─────────────────────────────────────────
    // DELETE /api/messages/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        messageService.deleteMessage(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Message deleted", null));
    }

    // ── Get enrolled students for instructor (for chat sidebar) ──────────────
    @GetMapping("/course/{courseId}/students")
    public ResponseEntity<List<MessageResponse.UserSummary>> getStudentsForChat(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<MessageResponse.UserSummary> students =
                messageService.getEnrolledStudentsForInstructor(courseId, userDetails.getUsername());
        return ResponseEntity.ok(students);
    }

    // ── Get instructor info for student (per course) ──────────────────────────
    @GetMapping("/course/{courseId}/instructor")
    public ResponseEntity<MessageResponse.UserSummary> getInstructorForCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        MessageResponse.UserSummary instructor =
                messageService.getInstructorForCourse(courseId, userDetails.getUsername());
        return ResponseEntity.ok(instructor);
    }

    // ── Get available chats list (for sidebar) ────────────────────────────────
    @GetMapping("/chats/student")
    public ResponseEntity<List<MessageResponse.CourseChat>> getStudentChats(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<MessageResponse.CourseChat> chats =
                messageService.getAvailableChatsForStudent(userDetails.getUsername());
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/chats/instructor")
    public ResponseEntity<List<MessageResponse.CourseChat>> getInstructorChats(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<MessageResponse.CourseChat> chats =
                messageService.getAvailableChatsForInstructor(userDetails.getUsername());
        return ResponseEntity.ok(chats);
    }
}