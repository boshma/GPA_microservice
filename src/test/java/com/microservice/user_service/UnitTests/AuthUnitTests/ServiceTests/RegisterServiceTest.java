package com.microservice.user_service.UnitTests.AuthUnitTests.ServiceTests;

import com.microservice.user_service.model.User;
import com.microservice.user_service.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterServiceTest extends BaseAuthServiceTest {

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void registerUser_Success() {
        User user = createTestUser();
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";
    
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
    
        User result = authService.registerUser(user);
    
        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(encodedPassword, result.getPassword());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    
        verify(userRepository).findByEmail(user.getEmail());
        verify(userRepository).findByUsername(user.getUsername());
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_DuplicateEmail() {
        User user = createTestUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(new User()));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.registerUser(user));

        assertEquals("Username or email already exists", exception.getMessage());
        verify(userRepository).findByEmail(user.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_DuplicateUsername() {
        User user = createTestUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(new User()));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.registerUser(user));

        assertEquals("Username or email already exists", exception.getMessage());
        verify(userRepository).findByEmail(user.getEmail());
        verify(userRepository).findByUsername(user.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }
}