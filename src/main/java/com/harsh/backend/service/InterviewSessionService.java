package com.harsh.backend.service;

import com.harsh.backend.dto.LeaderboardResponse;
import com.harsh.backend.dto.*;
import com.harsh.backend.entity.*;
import com.harsh.backend.exception.BadRequestException;
import com.harsh.backend.exception.ForbiddenException;
import com.harsh.backend.exception.ResourceNotFoundException;
import com.harsh.backend.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterviewSessionService {

    private final InterviewSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final SessionQuestionRepository sessionQuestionRepository;

    public InterviewSessionService(InterviewSessionRepository sessionRepository,
                                   UserRepository userRepository,
                                   QuestionRepository questionRepository,
                                   SessionQuestionRepository sessionQuestionRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.sessionQuestionRepository = sessionQuestionRepository;
    }

    public InterviewSessionResponse startSession(String email, InterviewSessionRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        InterviewSession session = new InterviewSession();
        session.setDomain(request.getDomain());
        session.setDifficulty(request.getDifficulty());
        session.setCreatedAt(LocalDateTime.now());
        session.setScore(0);
        session.setFinished(false);
        session.setUser(user);

        InterviewSession saved = sessionRepository.save(session);
        return toResponse(saved);
    }

    public List<InterviewSessionResponse> getHistory(String email) {
        return sessionRepository.findByUserEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::toResponse)
                .toList();
    }
    public List<LeaderboardResponse> getLeaderboard() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> {
                    List<InterviewSession> sessions = sessionRepository
                            .findByUserEmailOrderByCreatedAtDesc(user.getEmail());

                    int totalScore = sessions.stream()
                            .mapToInt(s -> s.getScore())
                            .sum();

                    String bestTopic = sessions.stream()
                            .collect(java.util.stream.Collectors.groupingBy(
                                    InterviewSession::getDomain,
                                    java.util.stream.Collectors.summingInt(
                                            s -> s.getScore()
                                    )
                            ))
                            .entrySet().stream()
                            .max(java.util.Map.Entry.comparingByValue())
                            .map(java.util.Map.Entry::getKey)
                            .orElse("N/A");

                    return new LeaderboardResponse(
                            user.getName(),
                            user.getEmail(),
                            totalScore,
                            sessions.size(),
                            bestTopic
                    );
                })
                .filter(l -> l.getTotalSessions() > 0)
                .sorted((a, b) -> b.getTotalScore() - a.getTotalScore())
                .toList();
    }

    public List<SessionQuestionResponse> getOrCreateMcqQuestions(Long sessionId, String email, int count) {

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        ensureOwner(session, email);

        if (session.isFinished()) {
            throw new IllegalStateException("Session is finished. You cannot assign new questions.");
        }

        List<SessionQuestion> existing = sessionQuestionRepository.findBySessionId(sessionId);
        if (!existing.isEmpty()) {
            return existing.stream().map(this::toSessionQuestionResponse).toList();
        }

        String topic = session.getDomain();   // using domain as topic filter
        String difficulty = session.getDifficulty();
        int safeCount = Math.max(count, 1);

        List<Question> questions = questionRepository.findTopByFilters(
                topic,
                difficulty,
                QuestionType.MCQ,
                safeCount
        );

        if (questions.isEmpty()) {
            throw new BadRequestException("No MCQ questions found for topic=" + topic + ", difficulty=" + difficulty);
        }

        List<SessionQuestion> saved = questions.stream().map(q -> {
            SessionQuestion sq = new SessionQuestion();
            sq.setSession(session);
            sq.setQuestion(q);
            sq.setSelectedAnswer(null);
            sq.setCorrect(null);
            sq.setMarksAwarded(null);
            return sessionQuestionRepository.save(sq);
        }).toList();

        return saved.stream().map(this::toSessionQuestionResponse).toList();
    }

    public SessionQuestionResponse getNextQuestion(Long sessionId, String email) {

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        ensureOwner(session, email);

        if (session.isFinished()) {
            throw new IllegalStateException("Session is finished. No more questions.");
        }

        List<SessionQuestion> list = sessionQuestionRepository.findBySessionIdOrderByIdAsc(sessionId);
        if (list.isEmpty()) {
            throw new BadRequestException("No questions assigned. Call /mcq first.");
        }

        return list.stream()
                .filter(sq -> sq.getSelectedAnswer() == null)
                .findFirst()
                .map(this::toSessionQuestionResponse)
                .orElseThrow(() -> new BadRequestException("All questions are already answered."));
    }

    public SessionProgressResponse getProgress(Long sessionId, String email) {

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        ensureOwner(session, email);

        int total = (int) sessionQuestionRepository.countBySessionId(sessionId);
        int attempted = (int) sessionQuestionRepository.countBySessionIdAndSelectedAnswerIsNotNull(sessionId);
        int remaining = Math.max(0, total - attempted);

        return new SessionProgressResponse(
                session.getId(),
                session.getDomain(),
                session.getDifficulty(),
                total,
                attempted,
                remaining,
                session.getScore(),
                session.isFinished()
        );
    }

    public SessionSummaryResponse getSummary(Long sessionId, String email) {

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        ensureOwner(session, email);

        int total = (int) sessionQuestionRepository.countBySessionId(sessionId);
        int attempted = (int) sessionQuestionRepository.countBySessionIdAndSelectedAnswerIsNotNull(sessionId);
        int correct = (int) sessionQuestionRepository.countBySessionIdAndCorrectTrue(sessionId);

        return new SessionSummaryResponse(
                session.getId(),
                session.getDomain(),
                session.getDifficulty(),
                session.getCreatedAt(),
                total,
                attempted,
                correct,
                session.getScore()
        );
    }

    public List<SessionReviewResponse> getReview(Long sessionId, String email) {

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        ensureOwner(session, email);

        return sessionQuestionRepository.findBySessionId(sessionId)
                .stream()
                .map(sq -> {
                    Question q = sq.getQuestion();
                    return new SessionReviewResponse(
                            q.getId(),
                            q.getQuestionText(),
                            q.getOptionA(),
                            q.getOptionB(),
                            q.getOptionC(),
                            q.getOptionD(),
                            q.getCorrectAnswer(),
                            sq.getSelectedAnswer(),
                            sq.getCorrect(),
                            sq.getMarksAwarded()
                    );
                })
                .toList();
    }

    public void finishSession(Long sessionId, String email) {

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        ensureOwner(session, email);

        session.setFinished(true);
        sessionRepository.save(session);
    }

    private void ensureOwner(InterviewSession session, String email) {
        if (session.getUser() == null || session.getUser().getEmail() == null ||
                !session.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("You are not allowed to access this session");
        }
    }

    private InterviewSessionResponse toResponse(InterviewSession s) {
        return new InterviewSessionResponse(
                s.getId(),
                s.getDomain(),
                s.getDifficulty(),
                s.getCreatedAt(),
                s.getScore(),
                s.getUser().getId(),
                s.getUser().getName(),
                s.getUser().getEmail()
        );
    }

    private SessionQuestionResponse toSessionQuestionResponse(SessionQuestion sq) {
        Question q = sq.getQuestion();
        return new SessionQuestionResponse(
                sq.getId(),
                q.getId(),
                q.getQuestionText(),
                q.getOptionA(),
                q.getOptionB(),
                q.getOptionC(),
                q.getOptionD()
        );
    }
}