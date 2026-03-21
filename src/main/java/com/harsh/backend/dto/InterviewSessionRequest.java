package com.harsh.backend.dto;

public class InterviewSessionRequest {

    private String domain;
    private String difficulty;

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}