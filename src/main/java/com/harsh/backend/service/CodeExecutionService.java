package com.harsh.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class CodeExecutionService {

    @Value("${judge0.api.key}")
    private String apiKey;

    @Value("${judge0.api.url}")
    private String apiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CodeExecutionResult execute(String sourceCode, int languageId, String stdin) {
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "language_id", languageId,
                    "source_code", sourceCode,
                    "stdin", stdin != null ? stdin : ""
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("x-rapidapi-host", "judge0-extra-ce1.p.rapidapi.com")
                    .header("x-rapidapi-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = objectMapper.readTree(response.body());

            String stdout = root.path("stdout").asText("");
            String stderr = root.path("stderr").asText("");
            String compileOutput = root.path("compile_output").asText("");
            String status = root.path("status").path("description").asText("");
            int statusId = root.path("status").path("id").asInt();

            return new CodeExecutionResult(stdout, stderr, compileOutput, status, statusId);

        } catch (Exception e) {
            return new CodeExecutionResult(null, e.getMessage(), null, "Error", 13);
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