package com.microservice.user_service.UnitTests.AuthUnitTests.ControllerTests;

import com.microservice.user_service.controller.AuthController;
import com.microservice.user_service.model.User;
import com.microservice.user_service.service.AuthService;
import com.microservice.user_service.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_Success() {

        User inputUser = createValidUser();
        User registeredUser = createValidUser();
        registeredUser.setId("testId");

        when(authService.registerUser(any(User.class))).thenReturn(registeredUser);

        ResponseEntity<?> response = authController.registerUser(inputUser);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("User registered successfully", responseBody.get("message"));
        assertEquals("testId", responseBody.get("userId"));
        verify(authService).registerUser(any(User.class));
    }

    @Test
    void registerUser_DuplicateUser() {

        User inputUser = createValidUser();
        when(authService.registerUser(any(User.class)))
                .thenThrow(new RuntimeException("Username or email already exists"));

        ResponseEntity<?> response = authController.registerUser(inputUser);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Username or email already exists", responseBody.get("error"));
        verify(authService).registerUser(any(User.class));
    }

    @Test
    void loginUser_Success() {

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "test@example.com");
        loginRequest.put("password", "password123");

        User authenticatedUser = createValidUser();
        authenticatedUser.setId("testId");

        when(authService.authenticateUser(anyString(), anyString())).thenReturn(authenticatedUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("test.jwt.token");

        ResponseEntity<?> response = authController.loginUser(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Login successful", responseBody.get("message"));
        assertEquals("test.jwt.token", responseBody.get("token"));
        assertEquals("testId", responseBody.get("userId"));
        verify(authService).authenticateUser(anyString(), anyString());
        verify(jwtUtil).generateToken(anyString());
    }

    @Test
    void loginUser_InvalidCredentials() {

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "test@example.com");
        loginRequest.put("password", "wrongpassword");

        when(authService.authenticateUser(anyString(), anyString()))
                .thenThrow(new RuntimeException("Invalid email or password"));

        ResponseEntity<?> response = authController.loginUser(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Invalid email or password", responseBody.get("error"));
        verify(authService).authenticateUser(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void loginUser_MissingCredentials() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "test@example.com");

        ResponseEntity<?> response = authController.loginUser(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(authService, never()).authenticateUser(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    private User createValidUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}