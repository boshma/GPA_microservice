package com.microservice.user_service.IntegrationTests.AuthIntegrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.microservice.user_service.IntegrationTests.AbstractIntegrationTest;
import com.microservice.user_service.IntegrationTests.HttpTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Assertions;

public class JwtIntegrationTest extends AbstractIntegrationTest {
    private HttpTestUtil httpTestUtil;
    private String jwtToken;
    private String userId;

    @BeforeEach
    @Override
    public void setUp() throws InterruptedException, IOException {
        super.setUp();
        httpTestUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, API_KEY);
        setupTestUser();
    }

    private void setupTestUser() throws IOException, InterruptedException {
        // Register a test user
        String registerJson = """
            {
                "username": "jwttest",
                "email": "jwt@test.com",
                "password": "Password123!"
            }""";

        httpTestUtil.sendRequest("POST", "/api/auth/register", registerJson);

        // Login to get JWT token
        String loginJson = """
            {
                "email": "jwt@test.com",
                "password": "Password123!"
            }""";

        HttpResponse<String> loginResponse = httpTestUtil.sendRequest("POST", "/api/auth/login", loginJson);
        JsonNode jsonResponse = httpTestUtil.parseResponse(loginResponse);
        jwtToken = jsonResponse.get("token").asText();
        userId = jsonResponse.get("userId").asText();
    }

    @Test
    public void accessProtectedEndpointWithValidToken() throws IOException, InterruptedException {
        HttpTestUtil authenticatedUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, API_KEY, jwtToken);
        
        HttpResponse<String> response = authenticatedUtil.sendRequest("GET", "/api/food", null);
        
        Assertions.assertEquals(200, response.statusCode(), 
            "Should successfully access protected endpoint with valid token");
    }

    @Test
    public void accessProtectedEndpointWithoutToken() throws IOException, InterruptedException {
        HttpResponse<String> response = httpTestUtil.sendRequest("GET", "/api/food", null);
        
        Assertions.assertEquals(401, response.statusCode(), 
            "Should fail to access protected endpoint without token");
    }

    @Test
    public void accessProtectedEndpointWithInvalidToken() throws IOException, InterruptedException {
        HttpTestUtil invalidTokenUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, API_KEY, "invalid.token.here");
        
        HttpResponse<String> response = invalidTokenUtil.sendRequest("GET", "/api/food", null);
        
        Assertions.assertEquals(401, response.statusCode(), 
            "Should fail to access protected endpoint with invalid token");
    }

    @Test
    public void accessProtectedEndpointWithExpiredToken() throws IOException, InterruptedException {
        String expiredToken = generateExpiredToken(userId);
        HttpTestUtil expiredTokenUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, API_KEY, expiredToken);
        
        HttpResponse<String> response = expiredTokenUtil.sendRequest("GET", "/api/food", null);
        
        Assertions.assertEquals(401, response.statusCode(), 
            "Should fail to access protected endpoint with expired token");
    }

    private String generateExpiredToken(String userId) {
        return getTestUtils().generateExpiredToken(userId);
    }
}