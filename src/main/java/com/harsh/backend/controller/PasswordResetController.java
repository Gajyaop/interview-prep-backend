package com.harsh.backend.controller;

import com.harsh.backend.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService resetService;

    public PasswordResetController(PasswordResetService resetService) {
        this.resetService = resetService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgot(@RequestBody Map<String, String> body) {
        resetService.sendResetEmail(body.get("email"));
        return ResponseEntity.ok("If an account exists, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> reset(@RequestBody Map<String, String> body) {
        resetService.resetPassword(body.get("token"), body.get("newPassword"));
        return ResponseEntity.ok("Password reset successfully.");
    }
}