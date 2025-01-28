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
import com.microservice.user_service.model.Food;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.LoggerFactory;

public class CreateFoodIntegrationTest extends BaseIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(CreateFoodIntegrationTest.class);
    private static final String API_KEY = "test_api_key";

    private ApplicationContext app;
    private HttpClient webClient;
    private ObjectMapper objectMapper;
    private String baseUrl;
    private String authToken;
    private String userId;

    @BeforeEach
    public void setUp() throws InterruptedException, IOException {
        clearDatabase();
        webClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // For LocalDate serialization

        // Set random port and API key
        System.setProperty("server.port", "0");
        System.setProperty("api.key", API_KEY);

        String[] args = new String[] {};
        app = SpringApplication.run(UserServiceApplication.class, args);

        // Get the actual port that was assigned
        baseUrl = "http://localhost:" + app.getEnvironment().getProperty("local.server.port");
        logger.info("Application started on port: " + app.getEnvironment().getProperty("local.server.port"));

        Thread.sleep(500);

        registerAndLoginUser();
    }

    private void registerAndLoginUser() throws IOException, InterruptedException {
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
        
        JsonNode registerResponseJson = objectMapper.readTree(registerResponse.body());
        userId = registerResponseJson.get("userId").asText();

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

        Food food = new Food();
        food.setName("Test Meal");
        food.setProtein(30.0);
        food.setCarb(40.0);
        food.setFat(20.0);
        food.setDate(LocalDate.now());
        food.setUserId(userId);

        String foodJson = objectMapper.writeValueAsString(food);
        logger.info("Request body: " + foodJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(foodJson))
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
            Food responseFood = objectMapper.readValue(response.body(), Food.class);
            Assertions.assertEquals("Test Meal", responseFood.getName());
            Assertions.assertEquals(30.0, responseFood.getProtein());
            Assertions.assertEquals(40.0, responseFood.getCarb());
            Assertions.assertEquals(20.0, responseFood.getFat());
            Assertions.assertNotNull(responseFood.getUserId(), "Response should contain a userId");
            Assertions.assertNotNull(responseFood.getId(), "Response should contain an id");
        }
    }

    @Test
    public void createFood_InvalidData() throws IOException, InterruptedException {
        logger.info("Starting createFood_InvalidData test");
        logger.info("Using auth token: " + authToken);

        Food invalidFood = new Food();
        invalidFood.setName(""); // Invalid empty name
        invalidFood.setProtein(-30.0); // Invalid negative value
        invalidFood.setCarb(40.0);
        invalidFood.setFat(20.0);
        invalidFood.setDate(LocalDate.now());
        invalidFood.setUserId(userId);

        String foodJson = objectMapper.writeValueAsString(invalidFood);
        logger.info("Request body: " + foodJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(foodJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        logger.info("Response status: " + response.statusCode());
        logger.info("Response body: " + response.body());

        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    @Test
    public void createFood_NoAuth() throws IOException, InterruptedException {
        Food food = new Food();
        food.setName("Test Meal");
        food.setProtein(30.0);
        food.setCarb(40.0);
        food.setFat(20.0);
        food.setDate(LocalDate.now());
        food.setUserId(userId);

        String foodJson = objectMapper.writeValueAsString(food);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(foodJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        Assertions.assertEquals(401, status, "Expected Status Code 401 - Actual Code was: " + status);
    }

    @Test
    public void createFood_NoApiKey() throws IOException, InterruptedException {
        Food food = new Food();
        food.setName("Test Meal");
        food.setProtein(30.0);
        food.setCarb(40.0);
        food.setFat(20.0);
        food.setDate(LocalDate.now());
        food.setUserId(userId);

        String foodJson = objectMapper.writeValueAsString(food);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(foodJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + authToken)
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        Assertions.assertEquals(401, status, "Expected Status Code 401 - Actual Code was: " + status);
    }
}