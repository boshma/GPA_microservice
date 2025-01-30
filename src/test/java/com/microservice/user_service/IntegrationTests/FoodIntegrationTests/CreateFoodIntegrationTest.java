package com.microservice.user_service.IntegrationTests.FoodIntegrationTests;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microservice.user_service.IntegrationTests.AbstractIntegrationTest;
import com.microservice.user_service.IntegrationTests.HttpTestUtil;
import com.microservice.user_service.model.Food;

public class CreateFoodIntegrationTest extends AbstractIntegrationTest {
    private HttpTestUtil httpTestUtil;
    private String authToken;
    private String userId;

    @BeforeEach
    @Override
    public void setUp() throws InterruptedException, IOException {
        super.setUp();
        objectMapper.registerModule(new JavaTimeModule()); // For LocalDate serialization
        httpTestUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, API_KEY);
        registerAndLoginUser();
    }

    private void registerAndLoginUser() throws IOException, InterruptedException {
        // Register
        String registerJson = """
                {
                    "username": "testuser",
                    "email": "test@example.com",
                    "password": "Password123!"
                }""";

        HttpResponse<String> registerResponse = httpTestUtil.sendRequest("POST", "/api/auth/register", registerJson);
        logger.info("Register response status: {}", registerResponse.statusCode());
        logger.info("Register response body: {}", registerResponse.body());
        
        JsonNode registerResponseJson = httpTestUtil.parseResponse(registerResponse);
        userId = registerResponseJson.get("userId").asText();

        // Login
        String loginJson = """
                {
                    "email": "test@example.com",
                    "password": "Password123!"
                }""";

        HttpResponse<String> loginResponse = httpTestUtil.sendRequest("POST", "/api/auth/login", loginJson);
        logger.info("Login response status: {}", loginResponse.statusCode());
        logger.info("Login response body: {}", loginResponse.body());

        JsonNode loginResponseJson = httpTestUtil.parseResponse(loginResponse);
        authToken = loginResponseJson.get("token").asText();
        logger.info("Auth token obtained: {}", authToken);
        
        // Update HttpTestUtil with auth token
        httpTestUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, API_KEY, authToken);
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
        logger.info("Request body: {}", foodJson);

        HttpResponse<String> response = httpTestUtil.sendRequest("POST", "/api/food", foodJson);

        logger.info("Response status: {}", response.statusCode());
        logger.info("Response body: {}", response.body());
        logger.info("Response headers: {}", response.headers().map());

        Assertions.assertEquals(201, response.statusCode(), 
            "Expected Status Code 201 - Actual Code was: " + response.statusCode() +
            "\nResponse body: " + response.body());

        Food responseFood = objectMapper.readValue(response.body(), Food.class);
        Assertions.assertEquals("Test Meal", responseFood.getName());
        Assertions.assertEquals(30.0, responseFood.getProtein());
        Assertions.assertEquals(40.0, responseFood.getCarb());
        Assertions.assertEquals(20.0, responseFood.getFat());
        Assertions.assertNotNull(responseFood.getUserId(), "Response should contain a userId");
        Assertions.assertNotNull(responseFood.getId(), "Response should contain an id");
    }

    @Test
    public void createFood_InvalidData() throws IOException, InterruptedException {
        logger.info("Starting createFood_InvalidData test");
        logger.info("Using auth token: {}", authToken);

        Food invalidFood = new Food();
        invalidFood.setName(""); // Invalid empty name
        invalidFood.setProtein(-30.0); // Invalid negative value
        invalidFood.setCarb(40.0);
        invalidFood.setFat(20.0);
        invalidFood.setDate(LocalDate.now());
        invalidFood.setUserId(userId);

        String foodJson = objectMapper.writeValueAsString(invalidFood);
        logger.info("Request body: {}", foodJson);

        HttpResponse<String> response = httpTestUtil.sendRequest("POST", "/api/food", foodJson);

        logger.info("Response status: {}", response.statusCode());
        logger.info("Response body: {}", response.body());

        Assertions.assertEquals(400, response.statusCode(), 
            "Expected Status Code 400 - Actual Code was: " + response.statusCode());
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
        
        // Create new HttpTestUtil instance without auth token
        HttpTestUtil noAuthHttpTestUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, API_KEY);
        HttpResponse<String> response = noAuthHttpTestUtil.sendRequest("POST", "/api/food", foodJson);

        Assertions.assertEquals(401, response.statusCode(), 
            "Expected Status Code 401 - Actual Code was: " + response.statusCode());
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

        // Create new HttpTestUtil instance without API key
        HttpTestUtil noApiKeyHttpTestUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, null, authToken);
        HttpResponse<String> response = noApiKeyHttpTestUtil.sendRequest("POST", "/api/food", foodJson);

        Assertions.assertEquals(401, response.statusCode(), 
            "Expected Status Code 401 - Actual Code was: " + response.statusCode());
    }
}