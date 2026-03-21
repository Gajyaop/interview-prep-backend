package com.harsh.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_case")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(nullable = false, length = 8000)
    private String input;

    @Column(nullable = false, length = 8000)
    private String expectedOutput;

    @Column(nullable = false)
    private boolean sample = false;  // true => show to user, false => hidden

    @Column(nullable = false)
    private int weight = 1;          // scoring weight
}