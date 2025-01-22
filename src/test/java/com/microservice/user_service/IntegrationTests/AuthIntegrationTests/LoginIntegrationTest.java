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
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;

import com.microservice.user_service.UserServiceApplication;
import com.microservice.user_service.IntegrationTests.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class LoginIntegrationTest extends BaseIntegrationTest {
    private ApplicationContext app;
    private HttpClient webClient;
    private ObjectMapper objectMapper;
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    public void setUp() throws InterruptedException {
        clearDatabase();
        webClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        String[] args = new String[] {};
        app = SpringApplication.run(UserServiceApplication.class, args);
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
                .uri(URI.create(BASE_URL + "/api/auth/register"))
                .POST(HttpRequest.BodyPublishers.ofString(registerJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        webClient.send(registerRequest, HttpResponse.BodyHandlers.ofString());

        String loginJson = """
            {
                "email": "test@example.com",
                "password": "Password123!"
            }""";

        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/auth/login"))
                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        HttpResponse<String> response = webClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        
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
                .uri(URI.create(BASE_URL + "/api/auth/login"))
                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        HttpResponse<String> response = webClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        
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
                .uri(URI.create(BASE_URL + "/api/auth/login"))
                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        HttpResponse<String> response = webClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        
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
                .uri(URI.create(BASE_URL + "/api/auth/login"))
                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        HttpResponse<String> response = webClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        
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
                .uri(URI.create(BASE_URL + "/api/auth/login"))
                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    
        HttpResponse<String> response = webClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        
        int status = response.statusCode();
        Assertions.assertEquals(401, status, "Expected Status Code 401 - Actual Code was: " + status);
        
        JsonNode jsonResponse = objectMapper.readTree(response.body());
        Assertions.assertEquals("Invalid email or password", jsonResponse.get("error").asText());
    }
}