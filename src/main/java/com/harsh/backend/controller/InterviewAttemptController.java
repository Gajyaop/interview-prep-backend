package com.harsh.backend.controller;

import com.harsh.backend.dto.SubmitAnswerRequest;
import com.harsh.backend.dto.SubmitAnswerResponse;
import com.harsh.backend.service.InterviewAttemptService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
public class InterviewAttemptController {

    private final InterviewAttemptService attemptService;

    public InterviewAttemptController(InterviewAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping("/{sessionId}/submit")
    public ResponseEntity<SubmitAnswerResponse> submit(
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitAnswerRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(attemptService.submitAnswer(sessionId, email, request));
    }
}