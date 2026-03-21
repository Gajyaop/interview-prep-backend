package com.harsh.backend.controller;

import com.harsh.backend.dto.QuestionCreateRequest;
import com.harsh.backend.dto.QuestionResponse;
import com.harsh.backend.dto.SubmitAnswerRequest;
import com.harsh.backend.dto.SubmitAnswerResponse;
import com.harsh.backend.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService service;

    public QuestionController(QuestionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> create(@Valid @RequestBody QuestionCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAll(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(service.getFiltered(topic, difficulty, type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<SubmitAnswerResponse> submitAnswer(
            @PathVariable Long id,
            @Valid @RequestBody SubmitAnswerRequest req) {
        return ResponseEntity.ok(service.submitAnswer(id, req));
    }

    @GetMapping("/topics")
    public ResponseEntity<List<String>> getTopics() {
        return ResponseEntity.ok(service.getTopics());
    }
}