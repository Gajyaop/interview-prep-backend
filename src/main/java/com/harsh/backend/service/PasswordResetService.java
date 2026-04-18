package com.harsh.backend.service;

import com.harsh.backend.entity.PasswordResetToken;
import com.harsh.backend.entity.User;
import com.harsh.backend.repository.PasswordResetTokenRepository;
import com.harsh.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void sendResetEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            tokenRepository.deleteByEmail(email);

            String token = UUID.randomUUID().toString();
            PasswordResetToken prt = new PasswordResetToken();
            prt.setToken(token);
            prt.setEmail(email);
            prt.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            tokenRepository.save(prt);

            if (mailSender != null) {
                String link = frontendUrl + "/reset-password?token=" + token;
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(email);
                msg.setSubject("Reset your Interview PrepPro password");
                msg.setText("Click to reset your password (valid 15 minutes):\n\n" + link +
                        "\n\nIf you didn't request this, ignore this email.");
                mailSender.send(msg);
            }
        });
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token"));

        if (prt.isUsed() || prt.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expired or already used");

        User user = userRepository.findByEmail(prt.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        prt.setUsed(true);
        tokenRepository.save(prt);
    }
}