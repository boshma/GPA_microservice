package com.microservice.user_service.IntegrationTests.AuthIntegrationTests;

import java.io.IOException;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.microservice.user_service.IntegrationTests.AbstractIntegrationTest;
import com.microservice.user_service.IntegrationTests.HttpTestUtil;

public class RegisterIntegrationTest extends AbstractIntegrationTest {
    private HttpTestUtil httpTestUtil;

    @BeforeEach
    @Override
    public void setUp() throws InterruptedException, IOException {
        super.setUp();
        httpTestUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, apiKey);
    }

    @Test
    public void registerSuccessful() throws IOException, InterruptedException {
        String registerJson = """
            {
                "username": "newuser",
                "email": "newuser@example.com",
                "password": "Password123!"
            }""";

        HttpResponse<String> response = httpTestUtil.sendRequest("POST", "/api/auth/register", registerJson);
        
        logger.info("Register response status: {}", response.statusCode());
        logger.info("Register response body: {}", response.body());

        Assertions.assertEquals(201, response.statusCode(), 
            "Expected Status Code 201 - Actual Code was: " + response.statusCode());

        JsonNode jsonResponse = httpTestUtil.parseResponse(response);
        Assertions.assertEquals("User registered successfully", jsonResponse.get("message").asText());
        Assertions.assertTrue(jsonResponse.has("userId"), "Response should contain a userId");
    }

    @Test
    public void registerDuplicateUser() throws IOException, InterruptedException {
        String registerJson = """
            {
                "username": "duplicate",
                "email": "duplicate@example.com",
                "password": "Password123!"
            }""";

        // First registration
        httpTestUtil.sendRequest("POST", "/api/auth/register", registerJson);

        // Second registration attempt
        HttpResponse<String> response = httpTestUtil.sendRequest("POST", "/api/auth/register", registerJson);
        
        logger.info("Register response status: {}", response.statusCode());
        logger.info("Register response body: {}", response.body());

        Assertions.assertEquals(409, response.statusCode(), 
            "Expected Status Code 409 - Actual Code was: " + response.statusCode());

        JsonNode jsonResponse = httpTestUtil.parseResponse(response);
        Assertions.assertEquals("Username or email already exists", jsonResponse.get("error").asText());
    }

    @Test
    public void registerWithMissingUsername() throws IOException, InterruptedException {
        String registerJson = """
            {
                "email": "test@example.com",
                "password": "Password123!"
            }""";

        HttpResponse<String> response = httpTestUtil.sendRequest("POST", "/api/auth/register", registerJson);
        
        logger.info("Register response status: {}", response.statusCode());
        logger.info("Register response body: {}", response.body());

        Assertions.assertEquals(400, response.statusCode(), 
            "Expected Status Code 400 - Actual Code was: " + response.statusCode());
    }

    @Test
    public void registerWithMissingEmail() throws IOException, InterruptedException {
        String registerJson = """
            {
                "username": "newuser",
                "password": "Password123!"
            }""";

        HttpResponse<String> response = httpTestUtil.sendRequest("POST", "/api/auth/register", registerJson);
        
        logger.info("Register response status: {}", response.statusCode());
        logger.info("Register response body: {}", response.body());

        Assertions.assertEquals(400, response.statusCode(), 
            "Expected Status Code 400 - Actual Code was: " + response.statusCode());
    }

    @Test
    public void registerWithMissingPassword() throws IOException, InterruptedException {
        String registerJson = """
            {
                "username": "newuser",
                "email": "test@example.com"
            }""";

        HttpResponse<String> response = httpTestUtil.sendRequest("POST", "/api/auth/register", registerJson);
        
        logger.info("Register response status: {}", response.statusCode());
        logger.info("Register response body: {}", response.body());

        Assertions.assertEquals(400, response.statusCode(), 
            "Expected Status Code 400 - Actual Code was: " + response.statusCode());
    }

    @Test
    public void registerWithEmptyUsername() throws IOException, InterruptedException {
        String registerJson = """
            {
                "username": "",
                "email": "test@example.com",
                "password": "Password123!"
            }""";

        HttpResponse<String> response = httpTestUtil.sendRequest("POST", "/api/auth/register", registerJson);
        
        logger.info("Register response status: {}", response.statusCode());
        logger.info("Register response body: {}", response.body());

        Assertions.assertEquals(400, response.statusCode(), 
            "Expected Status Code 400 - Actual Code was: " + response.statusCode());
    }

    @Test
    public void registerWithEmptyEmail() throws IOException, InterruptedException {
        String registerJson = """
            {
                "username": "newuser",
                "email": "",
                "password": "Password123!"
            }""";

        HttpResponse<String> response = httpTestUtil.sendRequest("POST", "/api/auth/register", registerJson);
        
        logger.info("Register response status: {}", response.statusCode());
        logger.info("Register response body: {}", response.body());

        Assertions.assertEquals(400, response.statusCode(), 
            "Expected Status Code 400 - Actual Code was: " + response.statusCode());
    }
}