package com.harsh.backend.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.harsh.backend.entity.User;
import com.harsh.backend.repository.UserRepository;
import com.harsh.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class GoogleAuthController {

    @Value("${google.client.id}")
    private String googleClientId;

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public GoogleAuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        String idTokenString = body.get("idToken");

        if (idTokenString == null || idTokenString.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing idToken"));
        }

        try {
            // Verify the Google token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid Google token"));
            }

            // Extract user info from Google token
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            // Find existing user or create new one
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name != null ? name : email.split("@")[0]);
                newUser.setPassword(""); // No password for Google users
                newUser.setProvider("GOOGLE");
                newUser.setProfilePicture(picture);
                return userRepository.save(newUser);
            });

            // Update profile picture if changed
            if (picture != null && !picture.equals(user.getProfilePicture())) {
                user.setProfilePicture(picture);
                userRepository.save(user);
            }

            // Generate your app's JWT token (same as normal login)
            String token = jwtUtil.generateToken(email);

            // Return same format as your normal login response
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("userId", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Google authentication failed: " + e.getMessage()));
        }
    }
}