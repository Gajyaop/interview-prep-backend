package com.harsh.backend.controller;

import com.harsh.backend.entity.Question;
import com.harsh.backend.service.AIQuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIQuestionService aiQuestionService;

    public AIController(AIQuestionService aiQuestionService) {
        this.aiQuestionService = aiQuestionService;
    }
    @PostMapping("/generate/coding")
    public ResponseEntity<GenerateResponse> generateCoding(
            @RequestBody GenerateRequest request) {

        List<Question> questions = aiQuestionService.generateCodingProblems(
                request.topic(),
                request.difficulty(),
                request.count()
        );

        return ResponseEntity.ok(new GenerateResponse(
                questions.size(),
                request.topic(),
                request.difficulty(),
                "Successfully generated " + questions.size() + " coding problems"
        ));
    }
    @PostMapping("/mock-interview/start")
    public ResponseEntity<MockInterviewResponse> startInterview(
            @RequestBody MockInterviewStartRequest request) {

        String questions = aiQuestionService.generateInterviewQuestions(
                request.topic(),
                request.role(),
                request.count()
        );

        return ResponseEntity.ok(new MockInterviewResponse(questions));
    }

    @PostMapping("/mock-interview/evaluate")
    public ResponseEntity<EvaluationResponse> evaluateAnswer(
            @RequestBody EvaluationRequest request) {

        String feedback = aiQuestionService.evaluateAnswer(
                request.question(),
                request.answer(),
                request.topic()
        );

        return ResponseEntity.ok(new EvaluationResponse(feedback));
    }

    @PostMapping("/mock-interview/summary")
    public ResponseEntity<SummaryResponse> getSummary(
            @RequestBody SummaryRequest request) {

        String summary = aiQuestionService.generateInterviewSummary(
                request.topic(),
                request.role(),
                request.questionsAndAnswers()
        );

        return ResponseEntity.ok(new SummaryResponse(summary));
    }

    public record MockInterviewStartRequest(String topic, String role, int count) {}
    public record MockInterviewResponse(String questionsJson) {}
    public record EvaluationRequest(String question, String answer, String topic) {}
    public record EvaluationResponse(String feedback) {}
    public record SummaryRequest(String topic, String role, String questionsAndAnswers) {}
    public record SummaryResponse(String summary) {}
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String reply = aiQuestionService.chat(request.message(), request.history());
        return ResponseEntity.ok(new ChatResponse(reply));
    }

    public record ChatRequest(String message, String history) {}
    public record ChatResponse(String reply) {}
    @PostMapping("/resume/analyze")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(
            @RequestBody ResumeAnalysisRequest request) {

        String analysis = aiQuestionService.analyzeResume(request.resumeText(), request.jobRole());
        return ResponseEntity.ok(new ResumeAnalysisResponse(analysis));
    }

    public record ResumeAnalysisRequest(String resumeText, String jobRole) {}
    public record ResumeAnalysisResponse(String analysis) {}
    @PostMapping("/review")
    public ResponseEntity<ReviewResponse> reviewCode(
            @RequestBody ReviewRequest request) {

        String feedback = aiQuestionService.reviewCode(
                request.code(),
                request.language(),
                request.problemDescription(),
                request.output()
        );

        return ResponseEntity.ok(new ReviewResponse(feedback));
    }

    public record ReviewRequest(
            String code,
            String language,
            String problemDescription,
            String output
    ) {}

    public record ReviewResponse(
            String feedback
    ) {}
    @PostMapping("/resume/generate-questions")
    public ResponseEntity<Map<String, Object>> generateResumeQuestions(
            @RequestBody Map<String, String> body) {

        String resumeText = body.get("resumeText");
        String targetRole = body.get("targetRole");
        String difficulty = body.getOrDefault("difficulty", "MEDIUM");

        String prompt = """
        You are an expert technical interviewer. Based on the following resume, 
        generate exactly 10 multiple choice interview questions for a %s position 
        at %s difficulty level.
        
        Resume:
        %s
        
        Rules:
        - Focus on skills, technologies, and experience mentioned in the resume
        - Mix technical questions about their specific tech stack
        - Include questions about their project experience
        - Include 1-2 behavioral questions based on their background
        - Each question must have exactly 4 options (A, B, C, D)
        - One correct answer per question
        
        Return ONLY a JSON array in this exact format, no other text:
        [
          {
            "question": "question text here",
            "options": ["option A", "option B", "option C", "option D"],
            "correctAnswer": "option A",
            "topic": "topic name",
            "explanation": "why this answer is correct"
          }
        ]
        """.formatted(targetRole, difficulty, resumeText);

        try {
            String aiResponse = aiQuestionService.callGeminiRaw(prompt);
            // Clean response — remove markdown code blocks if present
            String cleaned = aiResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            Map<String, Object> result = new HashMap<>();
            result.put("questions", cleaned);
            result.put("targetRole", targetRole);
            result.put("difficulty", difficulty);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to generate questions: " + e.getMessage()));
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<GenerateResponse> generate(
            @RequestBody GenerateRequest request) {

        List<Question> questions = aiQuestionService.generateAndSave(
                request.topic(),
                request.difficulty(),
                request.count()
        );

        return ResponseEntity.ok(new GenerateResponse(
                questions.size(),
                request.topic(),
                request.difficulty(),
                "Successfully generated " + questions.size() + " questions"
        ));
    }

    public record GenerateRequest(
            String topic,
            String difficulty,
            int count
    ) {}

    public record GenerateResponse(
            int generated,
            String topic,
            String difficulty,
            String message
    ) {}
}