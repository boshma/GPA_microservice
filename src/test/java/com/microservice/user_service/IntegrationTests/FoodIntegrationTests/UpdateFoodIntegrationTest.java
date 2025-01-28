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
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;

import com.microservice.user_service.UserServiceApplication;
import com.microservice.user_service.IntegrationTests.BaseIntegrationTest;
import com.microservice.user_service.model.Food;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class UpdateFoodIntegrationTest extends BaseIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(UpdateFoodIntegrationTest.class);
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
        objectMapper.registerModule(new JavaTimeModule());

        System.setProperty("server.port", "0");
        System.setProperty("api.key", API_KEY);

        String[] args = new String[] {};
        app = SpringApplication.run(UserServiceApplication.class, args);

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
        JsonNode loginResponseJson = objectMapper.readTree(loginResponse.body());
        authToken = loginResponseJson.get("token").asText();
        logger.info("Auth token obtained: " + authToken);
    }

    private String createTestFood(String name) throws IOException, InterruptedException {
        Food food = new Food();
        food.setName(name);
        food.setProtein(30.0);
        food.setCarb(40.0);
        food.setFat(20.0);
        food.setDate(LocalDate.now());
        food.setUserId(userId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(food)))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());
        Food createdFood = objectMapper.readValue(response.body(), Food.class);
        return createdFood.getId();
    }

    @Test
    public void updateFood_Success() throws IOException, InterruptedException {
        String foodId = createTestFood("Original Meal");
        logger.info("Created test food with ID: " + foodId);

        Food updatedFood = new Food();
        updatedFood.setName("Updated Meal");
        updatedFood.setProtein(50.0);
        updatedFood.setCarb(60.0);
        updatedFood.setFat(25.0);
        updatedFood.setDate(LocalDate.now());
        updatedFood.setUserId(userId);

        logger.info("Update request body: " + objectMapper.writeValueAsString(updatedFood));

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food/" + foodId))
                .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(updatedFood)))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(updateRequest, HttpResponse.BodyHandlers.ofString());

        logger.info("Update response status: " + response.statusCode());
        logger.info("Update response body: " + response.body());

        int status = response.statusCode();
        Assertions.assertEquals(200, status, "Expected Status Code 200");

        Food returnedFood = objectMapper.readValue(response.body(), Food.class);
        Assertions.assertEquals("Updated Meal", returnedFood.getName());
        Assertions.assertEquals(50.0, returnedFood.getProtein());
        Assertions.assertEquals(60.0, returnedFood.getCarb());
        Assertions.assertEquals(25.0, returnedFood.getFat());
        Assertions.assertEquals(userId, returnedFood.getUserId());
    }

    @Test
    public void updateFood_NotFound() throws IOException, InterruptedException {
        Food updatedFood = new Food();
        updatedFood.setName("Updated Meal");
        updatedFood.setProtein(50.0);
        updatedFood.setCarb(60.0);
        updatedFood.setFat(25.0);
        updatedFood.setDate(LocalDate.now());
        updatedFood.setUserId(userId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food/nonexistentid"))
                .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(updatedFood)))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        Assertions.assertEquals(404, status, "Expected Status Code 404");
    }

    @Test
    public void updateFood_InvalidData() throws IOException, InterruptedException {
        String foodId = createTestFood("Original Meal");

        Food invalidFood = new Food();
        invalidFood.setName(""); // Invalid empty name
        invalidFood.setProtein(-50.0); // Invalid negative value
        invalidFood.setCarb(60.0);
        invalidFood.setFat(25.0);
        invalidFood.setDate(LocalDate.now());
        invalidFood.setUserId(userId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food/" + foodId))
                .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(invalidFood)))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400");
    }

    @Test
    public void updateFood_Unauthorized() throws IOException, InterruptedException {
        String foodId = createTestFood("Original Meal");

        Food updatedFood = new Food();
        updatedFood.setName("Updated Meal");
        updatedFood.setProtein(50.0);
        updatedFood.setCarb(60.0);
        updatedFood.setFat(25.0);
        updatedFood.setDate(LocalDate.now());
        updatedFood.setUserId(userId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food/" + foodId))
                .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(updatedFood)))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        Assertions.assertEquals(401, status, "Expected Status Code 401");
    }

    @Test
    public void updateFood_DifferentUser() throws IOException, InterruptedException {
        String foodId = createTestFood("Original Meal");

        // Login as different user
        String registerJson = """
                {
                    "username": "testuser2",
                    "email": "test2@example.com",
                    "password": "Password123!"
                }""";

        HttpRequest registerRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/register"))
                .POST(HttpRequest.BodyPublishers.ofString(registerJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", API_KEY)
                .build();

        webClient.send(registerRequest, HttpResponse.BodyHandlers.ofString());

        String loginJson = """
                {
                    "email": "test2@example.com",
                    "password": "Password123!"
                }""";

        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/login"))
                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> loginResponse = webClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        String differentUserToken = objectMapper.readTree(loginResponse.body()).get("token").asText();

        Food updatedFood = new Food();
        updatedFood.setName("Updated Meal");
        updatedFood.setProtein(50.0);
        updatedFood.setCarb(60.0);
        updatedFood.setFat(25.0);
        updatedFood.setDate(LocalDate.now());
        updatedFood.setUserId(userId);

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food/" + foodId))
                .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(updatedFood)))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + differentUserToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(updateRequest, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        Assertions.assertEquals(403, status, "Expected Status Code 403");
    }
}