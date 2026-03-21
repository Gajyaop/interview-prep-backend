package com.harsh.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coding_submission")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CodingSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private InterviewSession session; // null if practice mode

    @Column(nullable = false)
    private String language; // "java", "python", etc

    @Column(nullable = false, length = 15000)
    private String code;

    @Column(nullable = false)
    private String status; // PENDING, ACCEPTED, WRONG_ANSWER, TLE, RUNTIME_ERROR, COMPILE_ERROR

    private Integer passed;
    private Integer total;
    private Integer score;

    @Column(length = 8000)
    private String error; // compile/runtime message (short)

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}