package com.harsh.backend.repository;

import com.harsh.backend.entity.SessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionQuestionRepository extends JpaRepository<SessionQuestion, Long> {

    Optional<SessionQuestion> findBySessionIdAndQuestionId(Long sessionId, Long questionId);

    List<SessionQuestion> findBySessionId(Long sessionId);

    List<SessionQuestion> findBySessionIdOrderByIdAsc(Long sessionId);

    long countBySessionId(Long sessionId);
    long countBySessionIdAndSelectedAnswerIsNotNull(Long sessionId);
    long countBySessionIdAndCorrectTrue(Long sessionId);
}