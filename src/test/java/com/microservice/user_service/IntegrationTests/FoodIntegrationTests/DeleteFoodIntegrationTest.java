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

public class DeleteFoodIntegrationTest extends BaseIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(DeleteFoodIntegrationTest.class);
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

        // Set random port and API key
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
    }

    private String createTestFood() throws IOException, InterruptedException {
        Food food = new Food();
        food.setName("Test Meal");
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
        JsonNode jsonResponse = objectMapper.readTree(response.body());
        return jsonResponse.get("id").asText();
    }

    @Test
    public void deleteFood_Success() throws IOException, InterruptedException {
        String foodId = createTestFood();
        logger.info("Created test food with ID: " + foodId);

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food/" + foodId))
                .DELETE()
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Delete response status: " + response.statusCode());
        int status = response.statusCode();
        Assertions.assertEquals(204, status, "Expected Status Code 204");

        // Verify food is deleted by trying to get it
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food/" + foodId))
                .GET()
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> getResponse = webClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(404, getResponse.statusCode(), "Food should not exist after deletion");
    }

    @Test
    public void deleteFood_NotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food/nonexistentid"))
                .DELETE()
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        int status = response.statusCode();
        Assertions.assertEquals(404, status, "Expected Status Code 404");
    }

    @Test
    public void deleteFood_Unauthorized_NoToken() throws IOException, InterruptedException {
        String foodId = createTestFood();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food/" + foodId))
                .DELETE()
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        int status = response.statusCode();
        Assertions.assertEquals(401, status, "Expected Status Code 401");

        // Verify food still exists
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food/" + foodId))
                .GET()
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> getResponse = webClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, getResponse.statusCode(), "Food should still exist");
    }

    @Test
    public void deleteFood_Unauthorized_NoApiKey() throws IOException, InterruptedException {
        String foodId = createTestFood();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food/" + foodId))
                .DELETE()
                .header("Authorization", "Bearer " + authToken)
                .build();

        HttpResponse<String> response = webClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        int status = response.statusCode();
        Assertions.assertEquals(401, status, "Expected Status Code 401");
    }

    @Test
    public void deleteFood_DifferentUser() throws IOException, InterruptedException {
        String foodId = createTestFood();

        // Create and login as different user
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

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/food/" + foodId))
                .DELETE()
                .header("Authorization", "Bearer " + differentUserToken)
                .header("X-API-Key", API_KEY)
                .build();

        HttpResponse<String> response = webClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        
        int status = response.statusCode();
        Assertions.assertEquals(403, status, "Expected Status Code 403");
    }
}