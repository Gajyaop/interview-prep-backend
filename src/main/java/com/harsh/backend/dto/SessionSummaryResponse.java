package com.harsh.backend.dto;

import java.time.LocalDateTime;

public class SessionSummaryResponse {
    private Long sessionId;
    private String domain;
    private String difficulty;
    private LocalDateTime createdAt;
    private int totalQuestions;
    private int attempted;
    private int correct;
    private int score;

    public SessionSummaryResponse(Long sessionId, String domain, String difficulty, LocalDateTime createdAt,
                                  int totalQuestions, int attempted, int correct, int score) {
        this.sessionId = sessionId;
        this.domain = domain;
        this.difficulty = difficulty;
        this.createdAt = createdAt;
        this.totalQuestions = totalQuestions;
        this.attempted = attempted;
        this.correct = correct;
        this.score = score;
    }

    public Long getSessionId() { return sessionId; }
    public String getDomain() { return domain; }
    public String getDifficulty() { return difficulty; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getAttempted() { return attempted; }
    public int getCorrect() { return correct; }
    public int getScore() { return score; }
}