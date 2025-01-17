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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LoginControllerTest {

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
    void loginUser_Success() {
        Map<String, String> loginRequest = createValidLoginRequest();
        User authenticatedUser = createValidUser();
        authenticatedUser.setId("testId");

        when(authService.authenticateUser(anyString(), anyString())).thenReturn(authenticatedUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("test.jwt.token");

        ResponseEntity<Map<String, String>> response = authController.loginUser(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, String> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Login successful", responseBody.get("message"));
        assertEquals("test.jwt.token", responseBody.get("token"));
        assertEquals("testId", responseBody.get("userId"));
        verify(authService).authenticateUser(anyString(), anyString());
        verify(jwtUtil).generateToken(anyString());
    }

    @Test
    void loginUser_InvalidCredentials() {
        Map<String, String> loginRequest = createValidLoginRequest();

        when(authService.authenticateUser(anyString(), anyString()))
                .thenThrow(new RuntimeException("Invalid email or password"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authController.loginUser(loginRequest));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid email or password", exception.getReason());
        verify(authService).authenticateUser(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void loginUser_MissingEmail() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("password", "password123");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authController.loginUser(loginRequest));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Email is required", exception.getReason());
        verify(authService, never()).authenticateUser(anyString(), anyString());
    }

    private Map<String, String> createValidLoginRequest() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "test@example.com");
        loginRequest.put("password", "password123");
        return loginRequest;
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