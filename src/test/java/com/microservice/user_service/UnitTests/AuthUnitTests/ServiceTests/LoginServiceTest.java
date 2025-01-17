package com.microservice.user_service.UnitTests.AuthUnitTests.ServiceTests;

import com.microservice.user_service.model.User;
import com.microservice.user_service.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginServiceTest extends BaseAuthServiceTest {

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void authenticateUser_Success() {
        User user = createTestUser();
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        User result = authService.authenticateUser(user.getEmail(), "password");

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository).findByEmail(user.getEmail());
        verify(passwordEncoder).matches("password", "encodedPassword");
    }

    @Test
    void authenticateUser_InvalidPassword() {
        User user = createTestUser();
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.authenticateUser(user.getEmail(), "wrongPassword"));

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmail(user.getEmail());
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
    }

    @Test
    void authenticateUser_UserNotFound() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.authenticateUser("test@test.com", "password"));

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmail("test@test.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void authenticateUser_NullEmail() {
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.authenticateUser(null, "password"));

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmail(null);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}