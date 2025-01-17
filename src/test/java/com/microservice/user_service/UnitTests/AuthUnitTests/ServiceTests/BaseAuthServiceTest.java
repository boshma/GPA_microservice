package com.microservice.user_service.UnitTests.AuthUnitTests.ServiceTests;

import com.microservice.user_service.model.User;
import com.microservice.user_service.repository.UserRepository;
import com.microservice.user_service.service.AuthService;
import org.mockito.Mock;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDateTime;

public abstract class BaseAuthServiceTest {
    
    protected AuthService authService;

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected BCryptPasswordEncoder passwordEncoder;

    protected User createTestUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}