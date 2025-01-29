package com.microservice.user_service.IntegrationTests.AuthIntegrationTests;

import java.io.IOException;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.microservice.user_service.IntegrationTests.AbstractIntegrationTest;
import com.microservice.user_service.IntegrationTests.HttpTestUtil;

public class LoginIntegrationTest extends AbstractIntegrationTest {
    private HttpTestUtil httpTestUtil;

    @BeforeEach
    @Override
    public void setUp() throws InterruptedException {
        super.setUp();
        httpTestUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, API_KEY);
    }

    @Test
    public void loginSuccessful() throws IOException, InterruptedException {
        // Register user first
        String registerJson = """
            {
                "username": "testuser",
                "email": "test@example.com",
                "password": "Password123!"
            }""";

        HttpResponse<String> registerResponse = httpTestUtil.sendRequest("POST", "/api/auth/register", registerJson);
        logger.info("Register response status: {}", registerResponse.statusCode());
        logger.info("Register response body: {}", registerResponse.body());

        // Login
        String loginJson = """
            {
                "email": "test@example.com",
                "password": "Password123!"
            }""";

        HttpResponse<String> response = httpTestUtil.sendRequest("POST", "/api/auth/login", loginJson);
        
        logger.info("Login response status: {}", response.statusCode());
        logger.info("Login response body: {}", response.body());

        Assertions.assertEquals(200, response.statusCode(), 
            "Expected Status Code 200 - Actual Code was: " + response.statusCode());

        JsonNode jsonResponse = httpTestUtil.parseResponse(response);
        Assertions.assertTrue(jsonResponse.has("token"), "Response should contain a token");
        Assertions.assertTrue(jsonResponse.has("userId"), "Response should contain a userId");
        Assertions.assertEquals("Login successful", jsonResponse.get("message").asText());
    }

    @Test
    public void loginWithInvalidCredentials() throws IOException, InterruptedException {
        String loginJson = """
            {
                "email": "nonexistent@example.com",
                "password": "wrongpassword"
            }""";

        HttpResponse<String> response = httpTestUtil.sendRequest("POST", "/api/auth/login", loginJson);
        
        logger.info("Login response status: {}", response.statusCode());
        logger.info("Login response body: {}", response.body());

        Assertions.assertEquals(401, response.statusCode(), 
            "Expected Status Code 401 - Actual Code was: " + response.statusCode());

        JsonNode jsonResponse = httpTestUtil.parseResponse(response);
        Assertions.assertEquals("Invalid email or password", jsonResponse.get("error").asText());
    }

    @Test
    public void loginWithMissingEmail() throws IOException, InterruptedException {
        String loginJson = """
            {
                "password": "password123"
            }""";

        HttpResponse<String> response = httpTestUtil.sendRequest("POST", "/api/auth/login", loginJson);
        
        logger.info("Login response status: {}", response.statusCode());
        logger.info("Login response body: {}", response.body());

        Assertions.assertEquals(400, response.statusCode(), 
            "Expected Status Code 400 - Actual Code was: " + response.statusCode());
    }

    @Test
    public void loginWithMissingPassword() throws IOException, InterruptedException {
        String loginJson = """
            {
                "email": "test@example.com"
            }""";

        HttpResponse<String> response = httpTestUtil.sendRequest("POST", "/api/auth/login", loginJson);
        
        logger.info("Login response status: {}", response.statusCode());
        logger.info("Login response body: {}", response.body());

        Assertions.assertEquals(400, response.statusCode(), 
            "Expected Status Code 400 - Actual Code was: " + response.statusCode());
    }

    @Test
    public void loginWithInvalidEmail() throws IOException, InterruptedException {
        String loginJson = """
            {
                "email": "notanemail",
                "password": "password123"
            }""";
    
        HttpResponse<String> response = httpTestUtil.sendRequest("POST", "/api/auth/login", loginJson);
        
        logger.info("Login response status: {}", response.statusCode());
        logger.info("Login response body: {}", response.body());

        Assertions.assertEquals(401, response.statusCode(), 
            "Expected Status Code 401 - Actual Code was: " + response.statusCode());
        
        JsonNode jsonResponse = httpTestUtil.parseResponse(response);
        Assertions.assertEquals("Invalid email or password", jsonResponse.get("error").asText());
    }
}