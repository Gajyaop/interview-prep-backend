package com.harsh.backend.dto;

public class LeaderboardResponse {
    private String name;
    private String email;
    private int totalScore;
    private int totalSessions;
    private String bestTopic;

    public LeaderboardResponse(String name, String email, int totalScore, int totalSessions, String bestTopic) {
        this.name = name;
        this.email = email;
        this.totalScore = totalScore;
        this.totalSessions = totalSessions;
        this.bestTopic = bestTopic;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getTotalScore() { return totalScore; }
    public int getTotalSessions() { return totalSessions; }
    public String getBestTopic() { return bestTopic; }
}