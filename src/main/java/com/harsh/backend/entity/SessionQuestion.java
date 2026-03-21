package com.harsh.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "session_questions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "question_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private InterviewSession session;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    private String selectedAnswer;

    private Boolean correct;

    @Column(name = "marks_awarded")
    private Integer marksAwarded;
}