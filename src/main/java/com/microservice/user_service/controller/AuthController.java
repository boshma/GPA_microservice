package com.microservice.user_service.controller;

import com.microservice.user_service.model.User;
import com.microservice.user_service.service.AuthService;
import com.microservice.user_service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = authService.registerUser(user);
            return ResponseEntity.status(201).body(Map.of(
                "message", "User registered successfully",
                "userId", registeredUser.getId()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            User authenticatedUser = authService.authenticateUser(email, password);
            String token = jwtUtil.generateToken(authenticatedUser.getId());
            return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "token", token,
                "userId", authenticatedUser.getId()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}