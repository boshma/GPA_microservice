package com.microservice.user_service.UnitTests;

import com.microservice.user_service.model.User;
import com.microservice.user_service.repository.UserRepository;
import com.microservice.user_service.service.AuthService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    void registerUser_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("password");

        // Mock repository responses
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.registerUser(user);

        // Validate response
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@test.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_DuplicateEmailOrUsername() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));

        User user = new User();
        user.setEmail("test@test.com");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.registerUser(user));
        assertEquals("Username or email already exists", exception.getMessage());
    }

    @Test
    void authenticateUser_ValidCredentials() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("encodedPassword");

        // Mock repository and password encoder responses
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        User result = authService.authenticateUser("test@test.com", "password");

        // Validate response
        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
        verify(passwordEncoder, times(1)).matches("password", "encodedPassword");
    }

    @Test
    void authenticateUser_InvalidCredentials() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("encodedPassword");

        // Mock repository and password encoder responses
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.authenticateUser("test@test.com", "wrongPassword"));
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void authenticateUser_UserNotFound() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.authenticateUser("test@test.com", "password"));
        assertEquals("Invalid email or password", exception.getMessage());
    }
}