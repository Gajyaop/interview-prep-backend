package com.harsh.backend.dto;

public class LoginResponse {

    private final String message;
    private final Long userId;
    private final String name;
    private final String email;
    private final String token;

    public LoginResponse(String message,
                         Long userId,
                         String name,
                         String email,
                         String token) {
        this.message = message;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.token = token;
    }

    public String getMessage() { return message; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getToken() { return token; }
}