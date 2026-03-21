package com.harsh.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitAnswerRequest(
        @NotNull Long questionId,
        @NotBlank String selectedAnswer
) {}