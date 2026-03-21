package com.harsh.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsh.backend.entity.Question;
import com.harsh.backend.entity.QuestionType;
import com.harsh.backend.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class AIQuestionService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIQuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<Question> generateAndSave(String topic, String difficulty, int count) {
        String prompt = buildPrompt(topic, difficulty, count);
        String responseText = callGemini(prompt);
        List<Question> questions = parseQuestions(responseText, topic, difficulty);
        return questionRepository.saveAll(questions);
    }

    private String buildPrompt(String topic, String difficulty, int count) {
        return String.format("""
                Generate exactly %d multiple choice questions about "%s" at %s difficulty level.
                
                Return ONLY a JSON array with no extra text, no markdown, no code blocks.
                Each object must have exactly these fields:
                {
                  "questionText": "the question",
                  "optionA": "first option",
                  "optionB": "second option",
                  "optionC": "third option",
                  "optionD": "fourth option",
                  "correctAnswer": "A or B or C or D",
                  "explanation": "brief explanation of why the answer is correct"
                }
                
                Rules:
                - Questions must be technical and accurate
                - Each question must have exactly 4 options
                - correctAnswer must be exactly one of: A, B, C, D
                - No duplicate questions
                - Difficulty %s means: %s
                """,
                count, topic, difficulty, difficulty,
                getDifficultyDescription(difficulty)
        );
    }

    private String getDifficultyDescription(String difficulty) {
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> "basic concepts, definitions, simple facts";
            case "MEDIUM" -> "intermediate concepts, application of knowledge";
            case "HARD" -> "advanced concepts, tricky edge cases, deep understanding";
            default -> "general knowledge";
        };
    }

    private String callGemini(String prompt) {
        try {
            String requestBody = objectMapper.writeValueAsString(
                    java.util.Map.of(
                            "contents", List.of(
                                    java.util.Map.of(
                                            "role", "user",
                                            "parts", List.of(
                                                    java.util.Map.of("text", prompt)
                                            )
                                    )
                            ),
                            "generationConfig", java.util.Map.of(
                                    "temperature", 1.0,
                                    "maxOutputTokens", 8192
                            )
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("=== GEMINI STATUS CODE: " + response.statusCode() + " ===");
            System.out.println("=== GEMINI FULL RESPONSE ===");
            System.out.println("Body length: " + response.body().length());
            System.out.println(response.body());
            System.out.println("============================");

            JsonNode root = objectMapper.readTree(response.body());

            if (root.has("error")) {
                throw new RuntimeException("Gemini API error: " + root.path("error").path("message").asText());
            }

            JsonNode candidates = root.path("candidates");
            if (candidates.isEmpty() || candidates.isNull()) {
                // Check for promptFeedback blockReason
                JsonNode feedback = root.path("promptFeedback");
                if (!feedback.isMissingNode()) {
                    throw new RuntimeException("Prompt blocked: " + feedback.toString());
                }
                throw new RuntimeException("No candidates in response: " + response.body());
            }

            return candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {
            throw new RuntimeException("Gemini API call failed: " + e.getMessage());
        }
    }
    public List<Question> generateCodingProblems(String topic, String difficulty, int count) {
        String prompt = buildCodingPrompt(topic, difficulty, count);
        String responseText = callGemini(prompt);
        List<Question> questions = parseCodingQuestions(responseText, topic, difficulty);
        return questionRepository.saveAll(questions);
    }

    private String buildCodingPrompt(String topic, String difficulty, int count) {
        return String.format("""
            Generate exactly %d coding problems for "%s" at %s difficulty level.
            
            Return ONLY a JSON array with no extra text, no markdown, no code blocks.
            Each object must have exactly these fields:
            {
              "questionText": "Full problem description including Input format, Output format, and Example",
              "referenceSolution": "Complete working solution code"
            }
            
            Rules:
            - Each problem must have clear Input/Output format
            - Include at least one example with input and output
            - Reference solution must be complete and working
            - Difficulty %s means: %s
            - Language for solution: %s
            """,
                count, topic, difficulty, difficulty,
                getDifficultyDescription(difficulty),
                topic
        );
    }

    private List<Question> parseCodingQuestions(String jsonText, String topic, String difficulty) {
        try {
            String cleaned = jsonText
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            JsonNode array = objectMapper.readTree(cleaned);
            List<Question> questions = new ArrayList<>();

            for (JsonNode node : array) {
                Question q = new Question();
                q.setType(QuestionType.CODING);
                q.setTopic(topic);
                q.setDifficulty(difficulty.toUpperCase());
                q.setQuestionText(node.path("questionText").asText());
                q.setReferenceSolution(node.path("referenceSolution").asText());
                questions.add(q);
            }

            return questions;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse coding questions: " + e.getMessage());
        }
    }
    public String reviewCode(String code, String language, String problemDescription, String output) {
        String prompt = String.format("""
            Review this %s code solution for the following problem:
            
            PROBLEM:
            %s
            
            CODE:
            %s
            
            ACTUAL OUTPUT:
            %s
            
            Please provide a brief code review covering:
            1. Correctness - Does it solve the problem?
            2. Time Complexity - What is the Big O?
            3. Code Quality - Is it clean and readable?
            4. Improvements - What could be done better?
            
            Keep the review concise and practical. Use simple language.
            Format with clear sections.
            """,
                language, problemDescription, code, output != null ? output : "No output yet"
        );

        return callGemini(prompt);
    }
    public String generateInterviewQuestions(String topic, String role, int count) {
        String prompt = String.format("""
            Generate exactly %d technical interview questions for a %s position focusing on %s.
            
            Return ONLY a JSON array of strings with no extra text, no markdown, no code blocks.
            Example format: ["Question 1?", "Question 2?", "Question 3?"]
            
            Questions should be:
            - Practical and commonly asked in real interviews
            - Mix of conceptual and problem-solving questions
            - Appropriate for a %s role
            """,
                count, role, topic, role
        );

        String response = callGemini(prompt);
        return response.replaceAll("```json", "").replaceAll("```", "").trim();
    }

    public String evaluateAnswer(String question, String answer, String topic) {
        String prompt = String.format("""
            Evaluate this interview answer for a %s technical interview question.
            
            QUESTION: %s
            
            CANDIDATE'S ANSWER: %s
            
            Provide a brief evaluation covering:
            1. Score: X/10
            2. What was good about the answer
            3. What was missing or could be improved
            4. A better/complete answer in 2-3 sentences
            
            Keep it concise and constructive. Max 150 words.
            """,
                topic, question, answer
        );

        return callGemini(prompt);
    }

    public String generateInterviewSummary(String topic, String role, String questionsAndAnswers) {
        String prompt = String.format("""
            Provide an overall assessment of this mock interview for a %s position focusing on %s.
            
            INTERVIEW TRANSCRIPT:
            %s
            
            Provide:
            1. Overall Score: X/10
            2. Key Strengths (2-3 points)
            3. Areas to Improve (2-3 points)
            4. Recommended Study Topics
            5. Final Verdict: Ready / Almost Ready / Needs More Preparation
            
            Keep it practical and encouraging. Max 200 words.
            """,
                role, topic, questionsAndAnswers
        );

        return callGemini(prompt);
    }
    public String chat(String message, String history) {
        String prompt = String.format("""
            You are a helpful technical interview preparation assistant for software developers.
            You help with: Java, Python, C++, Data Structures, Algorithms, OOP, SQL, DBMS,
            Operating Systems, Computer Networks, System Design, and general coding concepts.
            
            Keep answers concise, practical and easy to understand.
            Use examples where helpful.
            If asked non-technical questions, politely redirect to technical topics.
            
            %s
            
            User: %s
            Assistant:
            """,
                history != null && !history.isEmpty() ? "Previous conversation:\n" + history : "",
                message
        );
        return callGemini(prompt);
    }
    public String analyzeResume(String resumeText, String jobRole) {
        String prompt = String.format("""
            You are an expert technical recruiter and career coach. Analyze this resume for a %s position.
            
            RESUME:
            %s
            
            Provide a detailed analysis with these exact sections:
            
            1. OVERALL SCORE: X/10
            
            2. STRENGTHS (list 3-4 points)
            
            3. WEAKNESSES (list 3-4 points)
            
            4. MISSING SECTIONS (what's missing that recruiters expect)
            
            5. TOP IMPROVEMENTS (3-4 specific actionable suggestions)
            
            6. ATS SCORE: X/10 (how well it will pass Applicant Tracking Systems)
            
            7. VERDICT: Strong / Average / Needs Work
            
            Be specific, honest and constructive. Max 300 words.
            """,
                jobRole, resumeText
        );
        return callGemini(prompt);
    }

    private List<Question> parseQuestions(String jsonText, String topic, String difficulty) {
        try {
            // Clean up response — remove markdown code blocks if present
            String cleaned = jsonText
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            JsonNode array = objectMapper.readTree(cleaned);
            List<Question> questions = new ArrayList<>();

            for (JsonNode node : array) {
                Question q = new Question();
                q.setType(QuestionType.MCQ);
                q.setTopic(topic);
                q.setDifficulty(difficulty.toUpperCase());
                q.setQuestionText(node.path("questionText").asText());
                q.setOptionA(node.path("optionA").asText());
                q.setOptionB(node.path("optionB").asText());
                q.setOptionC(node.path("optionC").asText());
                q.setOptionD(node.path("optionD").asText());
                q.setCorrectAnswer(node.path("correctAnswer").asText());
                q.setReferenceSolution(node.path("explanation").asText());
                questions.add(q);
            }

            return questions;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage() + "\nResponse: " + jsonText);
        }
    }
}