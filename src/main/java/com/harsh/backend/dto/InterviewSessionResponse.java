package com.harsh.backend.dto;

import java.time.LocalDateTime;

public class InterviewSessionResponse {

    private Long id;
    private String domain;
    private String difficulty;
    private LocalDateTime createdAt;
    private int score;

    private Long userId;
    private String userName;
    private String userEmail;

    public InterviewSessionResponse() {}

    public InterviewSessionResponse(Long id, String domain, String difficulty,
                                    LocalDateTime createdAt, int score,
                                    Long userId, String userName, String userEmail) {
        this.id = id;
        this.domain = domain;
        this.difficulty = difficulty;
        this.createdAt = createdAt;
        this.score = score;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public Long getId() { return id; }
    public String getDomain() { return domain; }
    public String getDifficulty() { return difficulty; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getScore() { return score; }

    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }

    public void setId(Long id) { this.id = id; }
    public void setDomain(String domain) { this.domain = domain; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setScore(int score) { this.score = score; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}