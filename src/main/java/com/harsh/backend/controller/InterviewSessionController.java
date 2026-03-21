package com.harsh.backend.controller;

import com.harsh.backend.dto.*;
import com.harsh.backend.service.InterviewSessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.harsh.backend.dto.SessionProgressResponse;
import java.util.List;

@RestController
@RequestMapping("/api/interview")
public class InterviewSessionController {

    private final InterviewSessionService sessionService;

    public InterviewSessionController(InterviewSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/start")
    public ResponseEntity<InterviewSessionResponse> startSession(
            @Valid @RequestBody InterviewSessionRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(sessionService.startSession(email, request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<InterviewSessionResponse>> history(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(sessionService.getHistory(email));
    }

    @GetMapping("/{sessionId}/mcq")
    public ResponseEntity<List<SessionQuestionResponse>> getSessionMcqs(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "5") int count,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(sessionService.getOrCreateMcqQuestions(sessionId, email, count));
    }
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardResponse>> leaderboard() {
        return ResponseEntity.ok(sessionService.getLeaderboard());
    }

    @GetMapping("/{sessionId}/summary")
    public ResponseEntity<SessionSummaryResponse> summary(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(sessionService.getSummary(sessionId, email));
    }

    @GetMapping("/{sessionId}/review")
    public ResponseEntity<List<SessionReviewResponse>> review(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(sessionService.getReview(sessionId, email));
    }

    @PostMapping("/{sessionId}/finish")
    public ResponseEntity<String> finish(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        sessionService.finishSession(sessionId, email);
        return ResponseEntity.ok("Session finished");
    }
    @GetMapping("/{sessionId}/next")
    public ResponseEntity<SessionQuestionResponse> next(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(sessionService.getNextQuestion(sessionId, email));
    }

    @GetMapping("/{sessionId}/progress")
    public ResponseEntity<SessionProgressResponse> progress(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(sessionService.getProgress(sessionId, email));
    }
}