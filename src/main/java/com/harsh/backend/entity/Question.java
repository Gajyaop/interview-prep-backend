package com.harsh.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DB column: type (varchar)
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private QuestionType type;

    // DB column: topic (varchar)
    @Column(name = "topic", nullable = false)
    private String topic;

    // DB column: difficulty (varchar)
    @Column(name = "difficulty", nullable = false)
    private String difficulty;

    // DB column: question_text (varchar)
    @Column(name = "question_text", nullable = false, length = 2000)
    private String questionText;

    // DB columns are optiona/optionb/optionc/optiond (NO underscore)
    @Column(name = "optiona")
    private String optionA;

    @Column(name = "optionb")
    private String optionB;

    @Column(name = "optionc")
    private String optionC;

    @Column(name = "optiond")
    private String optionD;

    // DB column: correct_answer
    @Column(name = "correct_answer")
    private String correctAnswer;

    // DB column: reference_solution
    @Column(name = "reference_solution", length = 5000)
    private String referenceSolution;
}