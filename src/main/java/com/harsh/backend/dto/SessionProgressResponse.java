package com.harsh.backend.dto;

public class SessionProgressResponse {

    private Long sessionId;
    private String domain;
    private String difficulty;

    private int total;
    private int attempted;
    private int remaining;

    private int score;
    private boolean finished;

    public SessionProgressResponse(Long sessionId, String domain, String difficulty,
                                   int total, int attempted, int remaining,
                                   int score, boolean finished) {
        this.sessionId = sessionId;
        this.domain = domain;
        this.difficulty = difficulty;
        this.total = total;
        this.attempted = attempted;
        this.remaining = remaining;
        this.score = score;
        this.finished = finished;
    }

    public Long getSessionId() { return sessionId; }
    public String getDomain() { return domain; }
    public String getDifficulty() { return difficulty; }

    public int getTotal() { return total; }
    public int getAttempted() { return attempted; }
    public int getRemaining() { return remaining; }

    public int getScore() { return score; }
    public boolean isFinished() { return finished; }
}