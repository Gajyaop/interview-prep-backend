package com.harsh.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    private int rating;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}