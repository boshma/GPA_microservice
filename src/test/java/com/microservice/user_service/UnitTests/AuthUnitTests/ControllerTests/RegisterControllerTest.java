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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterControllerTest {

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

        ResponseEntity<Map<String, String>> response = authController.registerUser(inputUser);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<String, String> responseBody = response.getBody();
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

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authController.registerUser(inputUser));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Username or email already exists", exception.getReason());
        verify(authService).registerUser(any(User.class));
    }

    @Test
    void registerUser_MissingUsername() {
        User inputUser = createValidUser();
        inputUser.setUsername(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authController.registerUser(inputUser));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Username is required", exception.getReason());
        verify(authService, never()).registerUser(any(User.class));
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