package com.microservice.user_service.IntegrationTests;

import com.microservice.user_service.model.User;
import com.microservice.user_service.service.AuthService;
import com.microservice.user_service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TestUtils {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtUtil jwtUtil;

    private User currentTestUser;
    private static int userCounter = 0;

    public String getAuthToken() {
        if (currentTestUser == null) {
            createTestUser();
        }
        return jwtUtil.generateToken(currentTestUser.getId());
    }

    public String getCurrentUserId() {
        if (currentTestUser == null) {
            createTestUser();
        }
        return currentTestUser.getId();
    }

    private void createTestUser() {
        userCounter++;
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        
        User testUser = new User();
        testUser.setEmail("test" + userCounter + "_" + uniqueSuffix + "@example.com");
        testUser.setUsername("testuser" + userCounter + "_" + uniqueSuffix);
        testUser.setPassword("password123");
        
        currentTestUser = authService.registerUser(testUser);
    }

    public void clearCurrentUser() {
        currentTestUser = null;
    }

    public String generateExpiredToken(String userId) {
        return jwtUtil.generateTokenWithCustomExpiration(userId, -3600000); // expired 1 hour ago
    }
}