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
import com.fasterxml.jackson.databind.node.ObjectNode;

public class GetFoodIntegrationTest extends BaseIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(GetFoodIntegrationTest.class);
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

        // Set random port and API key
        System.setProperty("server.port", "0");
        System.setProperty("api.key", API_KEY);

        String[] args = new String[] {};
        app = SpringApplication.run(UserServiceApplication.class, args);

        // Get the actual port that was assigned
        baseUrl = "http://localhost:" + app.getEnvironment().getProperty("local.server.port");
        logger.info("Application started on port: " + app.getEnvironment().getProperty("local.server.port"));

        Thread.sleep(500);

        // Register and login test user
        registerAndLoginUser();
        
        // Create some test food entries
        createTestFood("Test Meal 1");
        createTestFood("Test Meal 2");
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
    }

    private void createTestFood(String name) throws IOException, InterruptedException {
        ObjectNode foodJson = objectMapper.createObjectNode()
                .put("name", name)
                .put("protein", 30.0)
                .put("carb", 40.0)
                .put("fat", 20.0)
                .put("date", LocalDate.now().toString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(foodJson.toString()))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", API_KEY)
                .build();

        webClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    public void getAllFood_Success() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food"))
                .GET()
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        Assertions.assertEquals(200, status);

        JsonNode jsonResponse = objectMapper.readTree(response.body());
        Assertions.assertEquals(2, jsonResponse.size());
        Assertions.assertEquals(userId, jsonResponse.get(0).get("userId").asText());
    }

    @Test
    public void getAllFood_WithDateFilter() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food?date=" + LocalDate.now().toString()))
                .GET()
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        Assertions.assertEquals(200, status);

        JsonNode jsonResponse = objectMapper.readTree(response.body());
        Assertions.assertEquals(2, jsonResponse.size());
    }

    @Test
    public void getFoodById_Success() throws IOException, InterruptedException {
        // First create a food item and get its ID
        ObjectNode foodJson = objectMapper.createObjectNode()
                .put("name", "Test Meal")
                .put("protein", 30.0)
                .put("carb", 40.0)
                .put("fat", 20.0)
                .put("date", LocalDate.now().toString());

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(foodJson.toString()))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> createResponse = webClient.send(createRequest, HttpResponse.BodyHandlers.ofString());
        JsonNode createResponseJson = objectMapper.readTree(createResponse.body());
        String foodId = createResponseJson.get("id").asText();

        // Now get the food item by ID
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food/" + foodId))
                .GET()
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        Assertions.assertEquals(200, status);

        JsonNode jsonResponse = objectMapper.readTree(response.body());
        Assertions.assertEquals("Test Meal", jsonResponse.get("name").asText());
        Assertions.assertEquals(userId, jsonResponse.get("userId").asText());
    }

    @Test
    public void getFoodById_NotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food/nonexistentid"))
                .GET()
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        Assertions.assertEquals(404, status);
    }

    @Test
    public void getFoodById_Unauthorized() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food/someid"))
                .GET()
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        Assertions.assertEquals(401, status);
    }
}