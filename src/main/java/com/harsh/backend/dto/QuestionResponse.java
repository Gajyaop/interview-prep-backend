package com.harsh.backend.dto;

import com.harsh.backend.entity.QuestionType;

public record QuestionResponse(
        Long id,
        QuestionType type,
        String topic,
        String difficulty,
        String questionText,
        String optionA,
        String optionB,
        String optionC,
        String optionD
) {}