package com.microservice.user_service.UnitTests;

import com.microservice.user_service.model.User;
import com.microservice.user_service.repository.UserRepository;
import com.microservice.user_service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void registerUser_Success() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("password");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = authService.registerUser(user);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@test.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());

        verify(userRepository, times(1)).save(any(User.class));
    }

    // Test for duplicate email or username
    @Test
    void registerUser_DuplicateEmailOrUsername() {
        // Arrange
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));

        User user = new User();
        user.setEmail("test@test.com");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.registerUser(user));
        assertEquals("Username or email already exists", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    // Test for successful authentication
    @Test
    void authenticateUser_ValidCredentials() {
        // Arrange
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        // Act
        User result = authService.authenticateUser("test@test.com", "password");

        // Assert
        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
        verify(passwordEncoder, times(1)).matches("password", "encodedPassword");
    }

    // Test for invalid credentials (wrong password)
    @Test
    void authenticateUser_InvalidCredentials() {
        // Arrange
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.authenticateUser("test@test.com", "wrongPassword"));
        assertEquals("Invalid email or password", exception.getMessage());

        verify(passwordEncoder, times(1)).matches("wrongPassword", "encodedPassword");
    }

    // Test for user not found during authentication
    @Test
    void authenticateUser_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.authenticateUser("test@test.com", "password"));
        assertEquals("Invalid email or password", exception.getMessage());

        verify(userRepository, times(1)).findByEmail("test@test.com");
    }
}