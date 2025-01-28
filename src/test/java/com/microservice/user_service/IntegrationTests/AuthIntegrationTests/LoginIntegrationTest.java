package com.microservice.user_service.IntegrationTests.AuthIntegrationTests;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;

import com.microservice.user_service.UserServiceApplication;
import com.microservice.user_service.IntegrationTests.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class LoginIntegrationTest extends BaseIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(LoginIntegrationTest.class);
    private static final String API_KEY = "test_api_key";

    private ApplicationContext app;
    private HttpClient webClient;
    private ObjectMapper objectMapper;
    private String baseUrl;

    @BeforeEach
    public void setUp() throws InterruptedException {
        clearDatabase();
        webClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();

        // Set random port and API key
        System.setProperty("server.port", "0");
        System.setProperty("api.key", API_KEY);

        String[] args = new String[] {};
        app = SpringApplication.run(UserServiceApplication.class, args);

        // Get the actual port that was assigned
        baseUrl = "http://localhost:" + app.getEnvironment().getProperty("local.server.port");
        logger.info("Application started on port: " + app.getEnvironment().getProperty("local.server.port"));

        Thread.sleep(500);
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        Thread.sleep(500);
        SpringApplication.exit(app);
    }

    @Test
    public void loginSuccessful() throws IOException, InterruptedException {
        String registerJson = """
            {
                "username": "testuser",
                "email": "test@example.com",
                "password": "Password123!"
            }""";

        HttpRequest registerRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/register"))
                .POST(HttpRequest.BodyPublishers.ofString(registerJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> registerResponse = webClient.send(registerRequest, HttpResponse.BodyHandlers.ofString());
        logger.info("Register response status: " + registerResponse.statusCode());
        logger.info("Register response body: " + registerResponse.body());

        String loginJson = """
            {
                "email": "test@example.com",
                "password": "Password123!"
            }""";

        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/login"))
                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Login response status: " + response.statusCode());
        logger.info("Login response body: " + response.body());

        int status = response.statusCode();
        Assertions.assertEquals(200, status, "Expected Status Code 200 - Actual Code was: " + status);

        JsonNode jsonResponse = objectMapper.readTree(response.body());
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

        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/login"))
                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Login response status: " + response.statusCode());
        logger.info("Login response body: " + response.body());

        int status = response.statusCode();
        Assertions.assertEquals(401, status, "Expected Status Code 401 - Actual Code was: " + status);

        JsonNode jsonResponse = objectMapper.readTree(response.body());
        Assertions.assertEquals("Invalid email or password", jsonResponse.get("error").asText());
    }

    @Test
    public void loginWithMissingEmail() throws IOException, InterruptedException {
        String loginJson = """
            {
                "password": "password123"
            }""";

        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/login"))
                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Login response status: " + response.statusCode());
        logger.info("Login response body: " + response.body());

        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    @Test
    public void loginWithMissingPassword() throws IOException, InterruptedException {
        String loginJson = """
            {
                "email": "test@example.com"
            }""";

        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/login"))
                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Login response status: " + response.statusCode());
        logger.info("Login response body: " + response.body());

        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    @Test
    public void loginWithInvalidEmail() throws IOException, InterruptedException {
        String loginJson = """
            {
                "email": "notanemail",
                "password": "password123"
            }""";
    
        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/login"))
                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", API_KEY)
                .build();
    
        HttpResponse<String> response = webClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Login response status: " + response.statusCode());
        logger.info("Login response body: " + response.body());

        int status = response.statusCode();
        Assertions.assertEquals(401, status, "Expected Status Code 401 - Actual Code was: " + status);
        
        JsonNode jsonResponse = objectMapper.readTree(response.body());
        Assertions.assertEquals("Invalid email or password", jsonResponse.get("error").asText());
    }
}