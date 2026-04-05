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

        // Guard: empty code
        if (sourceCode == null || sourceCode.isBlank()) {
            return new CodeExecutionResult(null, null,
                    "Please write your solution before running.", "No Code", 6);
        }

        String language = getLanguageName(languageId);

        // Guard: only default template, nothing written
        if (!hasUserCode(sourceCode, language)) {
            return new CodeExecutionResult(null, null,
                    "Please write your solution before running.", "No Solution Written", 6);
        }

        try {
            String prompt = buildPrompt(sourceCode, language, stdin);
            String url = geminiApiUrl + "?key=" + geminiApiKey;

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.1,
                            "maxOutputTokens", 1024
                    )
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            String output = extractText(response.getBody());

            if (output == null || output.isBlank()) {
                return new CodeExecutionResult(null, "Empty response from AI.", null, "Runtime Error", 11);
            }

            // Strip markdown backticks Gemini sometimes adds
            output = stripMarkdown(output);

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

    private boolean hasUserCode(String sourceCode, String language) {
        for (String line : sourceCode.split("\n")) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            if (t.startsWith("//") || t.startsWith("#")) continue;
            if (t.startsWith("import ")) continue;
            if (t.startsWith("#include") || t.startsWith("using namespace")) continue;
            if (t.equals("public class Main {")) continue;
            if (t.equals("public static void main(String[] args) {")) continue;
            if (t.equals("Scanner sc = new Scanner(System.in);")) continue;
            if (t.equals("int main() {")) continue;
            if (t.equals("return 0;")) continue;
            if (t.equals("}")) continue;
            return true; // found real user code
        }
        return false;
    }

    private String buildPrompt(String sourceCode, String language, String stdin) {
        String inputSection = (stdin != null && !stdin.isBlank())
                ? "stdin:\n" + stdin
                : "stdin: (none)";

        return """
            You are a code execution engine. Execute this %s code and return ONLY the output.
            
            STRICT RULES:
            - Return ONLY what the program prints to stdout
            - No markdown, no backticks, no explanation whatsoever
            - If compile error: respond ONLY with COMPILE_ERROR: <message>
            - If runtime error: respond ONLY with RUNTIME_ERROR: <message>
            - If no output: respond with (no output)
            
            %s
            
            Code:
            %s
            
            Output:""".formatted(language, inputSection, sourceCode);
    }

    private String extractText(Map<?, ?> responseBody) {
        try {
            // Check if response was blocked
            if (responseBody.containsKey("promptFeedback")) {
                Map<?, ?> feedback = (Map<?, ?>) responseBody.get("promptFeedback");
                if (feedback.containsKey("blockReason")) {
                    return "RUNTIME_ERROR: Request was blocked by safety filter.";
                }
            }

            List<?> candidates = (List<?>) responseBody.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return "RUNTIME_ERROR: No response received from AI.";
            }

            Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);

            // Check finish reason
            String finishReason = (String) candidate.get("finishReason");
            if ("SAFETY".equals(finishReason) || "RECITATION".equals(finishReason)) {
                return "RUNTIME_ERROR: Response blocked by safety filter.";
            }

            Map<?, ?> content = (Map<?, ?>) candidate.get("content");
            if (content == null) return "RUNTIME_ERROR: Empty content in AI response.";

            List<?> parts = (List<?>) content.get("parts");
            if (parts == null || parts.isEmpty()) return "RUNTIME_ERROR: Empty parts in AI response.";

            Object text = ((Map<?, ?>) parts.get(0)).get("text");
            return text != null ? text.toString().trim() : "RUNTIME_ERROR: Null text in AI response.";

        } catch (Exception e) {
            return "RUNTIME_ERROR: Could not parse AI response - " + e.getMessage();
        }
    }

    private String stripMarkdown(String output) {
        output = output.replaceAll("```[a-zA-Z]*\\n?", "");
        output = output.replaceAll("```", "");
        return output.trim();
    }

    private String getLanguageName(int languageId) {
        return switch (languageId) {
            case 62 -> "Java";
            case 71 -> "Python";
            case 54 -> "C++";
            case 63 -> "JavaScript";
            default -> "Unknown";
        };
    }

    public record CodeExecutionResult(
            String stdout,
            String stderr,
            String compileOutput,
            String status,
            int statusId
    ) {}
}