package com.harsh.backend.controller;

import com.harsh.backend.service.CodeExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;

    public CodeExecutionController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping("/run")
    public ResponseEntity<CodeRunResponse> runCode(
            @RequestBody CodeRunRequest request) {

        CodeExecutionService.CodeExecutionResult result = codeExecutionService.execute(
                request.sourceCode(),
                request.languageId(),
                request.stdin()
        );

        return ResponseEntity.ok(new CodeRunResponse(
                result.stdout(),
                result.stderr(),
                result.compileOutput(),
                result.status(),
                result.statusId()
        ));
    }

    public record CodeRunRequest(
            String sourceCode,
            int languageId,
            String stdin
    ) {}

    public record CodeRunResponse(
            String stdout,
            String stderr,
            String compileOutput,
            String status,
            int statusId
    ) {}
}