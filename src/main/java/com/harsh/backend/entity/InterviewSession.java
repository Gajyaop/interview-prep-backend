package com.harsh.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_session")
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String domain;
    private String difficulty;

    private LocalDateTime createdAt;

    private int score;

    @Column(nullable = false)
    private boolean finished = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Long getId() { return id; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}