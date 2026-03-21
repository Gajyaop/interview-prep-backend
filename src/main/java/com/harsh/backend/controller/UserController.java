package com.harsh.backend.controller;

import com.harsh.backend.dto.LoginRequest;
import com.harsh.backend.dto.LoginResponse;
import com.harsh.backend.dto.UserRequest;
import com.harsh.backend.dto.UserResponse;
import com.harsh.backend.service.UserService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // LOGIN (Public)
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(userService.login(request));
    }

    // REGISTER (Public)
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest request) {

        UserResponse savedUser = userService.createUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedUser);
    }

    // GET ALL USERS (Protected)
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET USER BY ID (Protected)
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(userService.getUserById(id));
    }

    // UPDATE USER (Protected)
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {

        return ResponseEntity.ok(
                userService.updateUser(id, request)
        );
    }

    // DELETE USER (Protected)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}