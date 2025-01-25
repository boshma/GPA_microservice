package com.microservice.user_service.IntegrationTests.FoodIntegrationTests;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;

import com.microservice.user_service.UserServiceApplication;
import com.microservice.user_service.IntegrationTests.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.LoggerFactory;

public class CreateFoodIntegrationTest extends BaseIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(CreateFoodIntegrationTest.class);
    private static final String API_KEY = "test_api_key";

    private ApplicationContext app;
    private HttpClient webClient;
    private ObjectMapper objectMapper;
    private String baseUrl;
    private String authToken;

    @BeforeEach
    public void setUp() throws InterruptedException, IOException {
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

        // First, register and login a test user
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

        HttpResponse<String> loginResponse = webClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        logger.info("Login response status: " + loginResponse.statusCode());
        logger.info("Login response body: " + loginResponse.body());

        JsonNode loginResponseJson = objectMapper.readTree(loginResponse.body());
        authToken = loginResponseJson.get("token").asText();
        logger.info("Auth token obtained: " + authToken);
    }

    @Test
    public void createFood_Success() throws IOException, InterruptedException {
        logger.info("Starting createFood_Success test");

        ObjectNode foodJson = objectMapper.createObjectNode()
                .put("name", "Test Meal")
                .put("protein", 30.0)
                .put("carb", 40.0)
                .put("fat", 20.0)
                .put("date", LocalDate.now().toString());

        logger.info("Request body: " + foodJson.toString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(foodJson.toString()))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        logger.info("Response status: " + response.statusCode());
        logger.info("Response body: " + response.body());
        logger.info("Response headers: " + response.headers().map());

        int status = response.statusCode();
        Assertions.assertEquals(201, status, "Expected Status Code 201 - Actual Code was: " + status +
                "\nResponse body: " + response.body());

        if (status == 201) {
            JsonNode jsonResponse = objectMapper.readTree(response.body());
            Assertions.assertEquals("Test Meal", jsonResponse.get("name").asText());
            Assertions.assertEquals(30.0, jsonResponse.get("protein").asDouble());
            Assertions.assertEquals(40.0, jsonResponse.get("carb").asDouble());
            Assertions.assertEquals(20.0, jsonResponse.get("fat").asDouble());
            Assertions.assertTrue(jsonResponse.has("userId"), "Response should contain a userId");
            Assertions.assertTrue(jsonResponse.has("id"), "Response should contain an id");
        }
    }

    @Test
    public void createFood_InvalidData() throws IOException, InterruptedException {
        logger.info("Starting createFood_InvalidData test");
        logger.info("Using auth token: " + authToken);

        ObjectNode foodJson = objectMapper.createObjectNode()
                .put("name", "") // Invalid empty name
                .put("protein", -30.0) // Invalid negative value
                .put("carb", 40.0)
                .put("fat", 20.0)
                .put("date", LocalDate.now().toString());

        logger.info("Request body: " + foodJson.toString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(foodJson.toString()))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", "test_api_key")
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        logger.info("Response status: " + response.statusCode());
        logger.info("Response body: " + response.body());

        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    @Test
    public void createFood_NoAuth() throws IOException, InterruptedException {
        ObjectNode foodJson = objectMapper.createObjectNode()
                .put("name", "Test Meal")
                .put("protein", 30.0)
                .put("carb", 40.0)
                .put("fat", 20.0)
                .put("date", LocalDate.now().toString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(foodJson.toString()))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        Assertions.assertEquals(401, status, "Expected Status Code 401 - Actual Code was: " + status);
    }

    @Test
    public void createFood_NoApiKey() throws IOException, InterruptedException {
        ObjectNode foodJson = objectMapper.createObjectNode()
                .put("name", "Test Meal")
                .put("protein", 30.0)
                .put("carb", 40.0)
                .put("fat", 20.0)
                .put("date", LocalDate.now().toString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(foodJson.toString()))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + authToken)
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        Assertions.assertEquals(401, status, "Expected Status Code 401 - Actual Code was: " + status);
    }
}