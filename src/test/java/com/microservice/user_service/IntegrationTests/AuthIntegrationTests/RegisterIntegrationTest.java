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

public class RegisterIntegrationTest extends BaseIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(RegisterIntegrationTest.class);
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
    public void registerSuccessful() throws IOException, InterruptedException {
        String registerJson = """
            {
                "username": "newuser",
                "email": "newuser@example.com",
                "password": "Password123!"
            }""";

        HttpRequest registerRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/register"))
                .POST(HttpRequest.BodyPublishers.ofString(registerJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(registerRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Register response status: " + response.statusCode());
        logger.info("Register response body: " + response.body());

        int status = response.statusCode();
        Assertions.assertEquals(201, status, "Expected Status Code 201 - Actual Code was: " + status);

        JsonNode jsonResponse = objectMapper.readTree(response.body());
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

        HttpRequest firstRegister = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/register"))
                .POST(HttpRequest.BodyPublishers.ofString(registerJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", API_KEY)
                .build();

        webClient.send(firstRegister, HttpResponse.BodyHandlers.ofString());

        HttpRequest secondRegister = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/register"))
                .POST(HttpRequest.BodyPublishers.ofString(registerJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(secondRegister, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Register response status: " + response.statusCode());
        logger.info("Register response body: " + response.body());

        int status = response.statusCode();
        Assertions.assertEquals(409, status, "Expected Status Code 409 - Actual Code was: " + status);

        JsonNode jsonResponse = objectMapper.readTree(response.body());
        Assertions.assertEquals("Username or email already exists", jsonResponse.get("error").asText());
    }

    @Test
    public void registerWithMissingUsername() throws IOException, InterruptedException {
        String registerJson = """
            {
                "email": "test@example.com",
                "password": "Password123!"
            }""";

        HttpRequest registerRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/register"))
                .POST(HttpRequest.BodyPublishers.ofString(registerJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(registerRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Register response status: " + response.statusCode());
        logger.info("Register response body: " + response.body());

        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    @Test
    public void registerWithMissingEmail() throws IOException, InterruptedException {
        String registerJson = """
            {
                "username": "newuser",
                "password": "Password123!"
            }""";

        HttpRequest registerRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/register"))
                .POST(HttpRequest.BodyPublishers.ofString(registerJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(registerRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Register response status: " + response.statusCode());
        logger.info("Register response body: " + response.body());

        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    @Test
    public void registerWithMissingPassword() throws IOException, InterruptedException {
        String registerJson = """
            {
                "username": "newuser",
                "email": "test@example.com"
            }""";

        HttpRequest registerRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/register"))
                .POST(HttpRequest.BodyPublishers.ofString(registerJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(registerRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Register response status: " + response.statusCode());
        logger.info("Register response body: " + response.body());

        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    @Test
    public void registerWithEmptyUsername() throws IOException, InterruptedException {
        String registerJson = """
            {
                "username": "",
                "email": "test@example.com",
                "password": "Password123!"
            }""";

        HttpRequest registerRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/register"))
                .POST(HttpRequest.BodyPublishers.ofString(registerJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(registerRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Register response status: " + response.statusCode());
        logger.info("Register response body: " + response.body());

        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    @Test
    public void registerWithEmptyEmail() throws IOException, InterruptedException {
        String registerJson = """
            {
                "username": "newuser",
                "email": "",
                "password": "Password123!"
            }""";

        HttpRequest registerRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/register"))
                .POST(HttpRequest.BodyPublishers.ofString(registerJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(registerRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Register response status: " + response.statusCode());
        logger.info("Register response body: " + response.body());

        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }
}