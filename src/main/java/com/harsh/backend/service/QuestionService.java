package com.harsh.backend.service;

import com.harsh.backend.dto.*;
import com.harsh.backend.entity.Question;
import com.harsh.backend.entity.QuestionType;
import com.harsh.backend.exception.ResourceNotFoundException;
import com.harsh.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository repo;

    public QuestionService(QuestionRepository repo) {
        this.repo = repo;
    }

    public QuestionResponse create(QuestionCreateRequest req) {
        Question q = new Question();
        q.setType(req.type());
        q.setTopic(req.topic());
        q.setDifficulty(req.difficulty());
        q.setQuestionText(req.questionText());
        q.setOptionA(req.optionA());
        q.setOptionB(req.optionB());
        q.setOptionC(req.optionC());
        q.setOptionD(req.optionD());
        q.setCorrectAnswer(req.correctAnswer());
        return toResponse(repo.save(q));
    }

    public List<QuestionResponse> getFiltered(String topic, String difficulty, String type) {
        List<Question> questions;

        if (topic != null && difficulty != null) {
            questions = repo.findByTopicAndDifficulty(topic, difficulty);
        } else if (topic != null) {
            questions = repo.findByTopic(topic);
        } else if (difficulty != null) {
            questions = repo.findByDifficulty(difficulty);
        } else if (type != null) {
            questions = repo.findByType(QuestionType.valueOf(type.toUpperCase()));
        } else {
            questions = repo.findAll();
        }

        return questions.stream().map(this::toResponse).toList();
    }

    public QuestionResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public SubmitAnswerResponse submitAnswer(Long id, SubmitAnswerRequest req) {
        Question q = findOrThrow(id);
        boolean correct = q.getCorrectAnswer()
                .trim()
                .equalsIgnoreCase(req.selectedAnswer().trim());
        return new SubmitAnswerResponse(
                correct,
                q.getCorrectAnswer(),
                correct ? "Correct!" : "The correct answer is: " + q.getCorrectAnswer()
        );
    }

    public List<String> getTopics() {
        return repo.findDistinctTopics();
    }

    private Question findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + id));
    }

    private QuestionResponse toResponse(Question q) {
        return new QuestionResponse(
                q.getId(),
                q.getType(),
                q.getTopic(),
                q.getDifficulty(),
                q.getQuestionText(),
                q.getOptionA(),
                q.getOptionB(),
                q.getOptionC(),
                q.getOptionD()
        );
    }
}