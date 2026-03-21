package com.harsh.backend.controller;

import com.harsh.backend.entity.Feedback;
import com.harsh.backend.repository.FeedbackRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;

    public FeedbackController(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @PostMapping
    public ResponseEntity<String> submitFeedback(
            @Valid @RequestBody FeedbackRequest request) {

        Feedback feedback = new Feedback();
        feedback.setName(request.name());
        feedback.setEmail(request.email());
        feedback.setSubject(request.subject());
        feedback.setMessage(request.message());
        feedback.setRating(request.rating());
        feedback.setCreatedAt(LocalDateTime.now());

        feedbackRepository.save(feedback);

        return ResponseEntity.ok("Feedback submitted successfully");
    }

    public record FeedbackRequest(
            String name,
            String email,
            String subject,
            String message,
            int rating
    ) {}
}