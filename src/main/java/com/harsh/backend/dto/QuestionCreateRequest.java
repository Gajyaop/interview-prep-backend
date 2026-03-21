package com.harsh.backend.dto;

import com.harsh.backend.entity.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuestionCreateRequest(
        @NotNull QuestionType type,
        @NotBlank String topic,
        @NotBlank String difficulty,
        @NotBlank String questionText,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String correctAnswer
) {}