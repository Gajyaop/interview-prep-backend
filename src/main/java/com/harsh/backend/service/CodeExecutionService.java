package com.harsh.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class CodeExecutionService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public CodeExecutionResult execute(String sourceCode, int languageId, String stdin) {
        try {
            String prompt = buildPrompt(sourceCode, languageId, stdin);

            String url = geminiApiUrl + "?key=" + geminiApiKey;

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.1
                    )
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            String output = extractText(response.getBody());

            if (output.startsWith("COMPILE_ERROR:")) {
                String errorMsg = output.replace("COMPILE_ERROR:", "").trim();
                return new CodeExecutionResult(null, null, errorMsg, "Compilation Error", 6);
            } else if (output.startsWith("RUNTIME_ERROR:")) {
                String errorMsg = output.replace("RUNTIME_ERROR:", "").trim();
                return new CodeExecutionResult(null, errorMsg, null, "Runtime Error", 11);
            } else {
                return new CodeExecutionResult(output, null, null, "Accepted", 3);
            }

        } catch (Exception e) {
            return new CodeExecutionResult(
                    null,
                    "Execution failed: " + e.getMessage(),
                    null,
                    "Internal Error",
                    13
            );
        }
    }

    private String buildPrompt(String sourceCode, int languageId, String stdin) {
        String language = switch (languageId) {
            case 62 -> "Java";
            case 71 -> "Python";
            case 54 -> "C++";
            case 63 -> "JavaScript";
            default -> "Unknown";
        };

        String inputSection = (stdin != null && !stdin.isBlank())
                ? "stdin:\n" + stdin
                : "stdin: (none)";

        return """
            You are a precise code execution engine for %s.
            
            Rules (follow strictly):
            - Return ONLY the program output, nothing else
            - No markdown, no backticks, no explanation
            - If compile error → respond ONLY with: COMPILE_ERROR: <error message>
            - If runtime error → respond ONLY with: RUNTIME_ERROR: <error message>
            - Simulate execution exactly as a real %s runtime would
            
            %s
            
            Code:
            %s
            
            Output:""".formatted(language, language, inputSection, sourceCode);
    }

    private String extractText(Map<?, ?> responseBody) {
        try {
            var candidates = (List<?>) responseBody.get("candidates");
            var content = (Map<?, ?>) ((Map<?, ?>) candidates.get(0)).get("content");
            var parts = (List<?>) content.get("parts");
            return ((Map<?, ?>) parts.get(0)).get("text").toString().trim();
        } catch (Exception e) {
            return "RUNTIME_ERROR: Could not parse AI response";
        }
    }

    public record CodeExecutionResult(
            String stdout,
            String stderr,
            String compileOutput,
            String status,
            int statusId
    ) {}
}