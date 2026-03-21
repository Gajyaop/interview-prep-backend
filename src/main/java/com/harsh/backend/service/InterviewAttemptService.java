package com.harsh.backend.service;

import com.harsh.backend.dto.SubmitAnswerRequest;
import com.harsh.backend.dto.SubmitAnswerResponse;
import com.harsh.backend.entity.InterviewSession;
import com.harsh.backend.entity.Question;
import com.harsh.backend.entity.SessionQuestion;
import com.harsh.backend.repository.InterviewSessionRepository;
import com.harsh.backend.repository.SessionQuestionRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class InterviewAttemptService {

    private final InterviewSessionRepository sessionRepository;
    private final SessionQuestionRepository sessionQuestionRepository;

    public InterviewAttemptService(InterviewSessionRepository sessionRepository,
                                   SessionQuestionRepository sessionQuestionRepository) {
        this.sessionRepository = sessionRepository;
        this.sessionQuestionRepository = sessionQuestionRepository;
    }

    public SubmitAnswerResponse submitAnswer(Long sessionId, String email, SubmitAnswerRequest request) {

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (session.getUser() == null || session.getUser().getEmail() == null
                || !session.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("You are not allowed to access this session");
        }

        if (session.isFinished()) {
            throw new RuntimeException("Session already finished. You cannot submit answers now.");
        }

        SessionQuestion sq = sessionQuestionRepository
                .findBySessionIdAndQuestionId(sessionId, request.questionId())
                .orElseThrow(() -> new RuntimeException("Question not found in this session"));

        if (sq.getSelectedAnswer() != null) {
            throw new RuntimeException("This question is already answered in this session");
        }

        Question q = sq.getQuestion();

        if (request.selectedAnswer() == null || request.selectedAnswer().isBlank()) {
            throw new RuntimeException("selectedAnswer is required");
        }

        if (q.getCorrectAnswer() == null || q.getCorrectAnswer().isBlank()) {
            throw new RuntimeException("Question correctAnswer is missing in DB for questionId=" + q.getId());
        }

        String selected = request.selectedAnswer().trim().toUpperCase(Locale.ROOT);
        String correctAnswer = q.getCorrectAnswer().trim().toUpperCase(Locale.ROOT);

        boolean isCorrect = selected.equals(correctAnswer);
        int marks = isCorrect ? marksByDifficulty(session.getDifficulty()) : 0;

        sq.setSelectedAnswer(selected);
        sq.setCorrect(isCorrect);
        sq.setMarksAwarded(marks);
        sessionQuestionRepository.save(sq);

        int totalScore = sessionQuestionRepository.findBySessionId(sessionId).stream()
                .map(SessionQuestion::getMarksAwarded)
                .filter(m -> m != null)
                .mapToInt(Integer::intValue)
                .sum();

        session.setScore(totalScore);
        sessionRepository.save(session);

        return new SubmitAnswerResponse(
                isCorrect,
                q.getCorrectAnswer(),
                isCorrect ? "Correct!" : "The correct answer is: " + q.getCorrectAnswer()
        );
    }

    private int marksByDifficulty(String difficulty) {
        if (difficulty == null) return 1;
        return switch (difficulty.trim().toLowerCase(Locale.ROOT)) {
            case "easy" -> 1;
            case "medium" -> 2;
            case "hard" -> 3;
            default -> 1;
        };
    }
}