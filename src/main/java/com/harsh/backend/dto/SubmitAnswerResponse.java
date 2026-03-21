package com.harsh.backend.dto;

public record SubmitAnswerResponse(
        boolean correct,
        String correctAnswer,
        String explanation
) {}