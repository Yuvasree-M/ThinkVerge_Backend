package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.request.FeedbackRequest;
import com.thinkverge.lms.dto.response.FeedbackResponse;
import com.thinkverge.lms.model.Feedback;
import com.thinkverge.lms.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;

    // ── PUBLIC: Submit feedback ──────────────────────────────
    @PostMapping
    public ResponseEntity<FeedbackResponse> submit(@RequestBody FeedbackRequest request) {
        Feedback feedback = Feedback.builder()
                .name(request.getName() != null && !request.getName().isBlank()
                        ? request.getName() : "Anonymous")
                .message(request.getMessage())
                .rating(request.getRating())
                .courseTitle(request.getCourseTitle())
                .approved(false)
                .build();
        Feedback saved = feedbackRepository.save(feedback);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    // ── PUBLIC: Get all approved feedback (for landing page testimonials) ──
    @GetMapping("/approved")
    public List<FeedbackResponse> approved() {
        return feedbackRepository.findByApprovedTrueOrderBySubmittedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── ADMIN: Get all pending feedback ──────────────────────
    @GetMapping("/pending")
    public List<FeedbackResponse> pending() {
        return feedbackRepository.findByApprovedFalseOrderBySubmittedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── ADMIN: Approve feedback ───────────────────────────────
    @PutMapping("/{id}/approve")
    public ResponseEntity<FeedbackResponse> approve(@PathVariable Long id) {
        return feedbackRepository.findById(id)
                .map(f -> {
                    f.setApproved(true);
                    return ResponseEntity.ok(toResponse(feedbackRepository.save(f)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── ADMIN: Delete feedback ────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!feedbackRepository.existsById(id)) return ResponseEntity.notFound().build();
        feedbackRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private FeedbackResponse toResponse(Feedback f) {
        return FeedbackResponse.builder()
                .id(f.getId())
                .name(f.getName())
                .message(f.getMessage())
                .rating(f.getRating())
                .courseTitle(f.getCourseTitle())
                .approved(f.isApproved())
                .submittedAt(f.getSubmittedAt())
                .build();
    }
}