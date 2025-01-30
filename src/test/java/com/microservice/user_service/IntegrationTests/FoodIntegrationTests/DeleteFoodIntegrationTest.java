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

public class DeleteFoodIntegrationTest extends AbstractIntegrationTest {
    private HttpTestUtil httpTestUtil;
    private String authToken;
    private String userId;

    @BeforeEach
    @Override
    public void setUp() throws InterruptedException, IOException {
        super.setUp();
        objectMapper.registerModule(new JavaTimeModule());
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
        JsonNode registerResponseJson = httpTestUtil.parseResponse(registerResponse);
        userId = registerResponseJson.get("userId").asText();

        // Login
        String loginJson = """
                {
                    "email": "test@example.com",
                    "password": "Password123!"
                }""";

        HttpResponse<String> loginResponse = httpTestUtil.sendRequest("POST", "/api/auth/login", loginJson);
        JsonNode loginResponseJson = httpTestUtil.parseResponse(loginResponse);
        authToken = loginResponseJson.get("token").asText();
        
        // Update HttpTestUtil with auth token
        httpTestUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, API_KEY, authToken);
    }

    private String createTestFood() throws IOException, InterruptedException {
        Food food = new Food();
        food.setName("Test Meal");
        food.setProtein(30.0);
        food.setCarb(40.0);
        food.setFat(20.0);
        food.setDate(LocalDate.now());
        food.setUserId(userId);

        HttpResponse<String> response = httpTestUtil.sendRequest("POST", "/api/food", 
            objectMapper.writeValueAsString(food));
        JsonNode jsonResponse = objectMapper.readTree(response.body());
        return jsonResponse.get("id").asText();
    }

    @Test
    public void deleteFood_Success() throws IOException, InterruptedException {
        String foodId = createTestFood();
        logger.info("Created test food with ID: {}", foodId);

        HttpResponse<String> response = httpTestUtil.sendRequest("DELETE", "/api/food/" + foodId, null);
        
        logger.info("Delete response status: {}", response.statusCode());
        Assertions.assertEquals(204, response.statusCode(), "Expected Status Code 204");

        // Verify food is deleted
        HttpResponse<String> getResponse = httpTestUtil.sendRequest("GET", "/api/food/" + foodId, null);
        Assertions.assertEquals(404, getResponse.statusCode(), "Food should not exist after deletion");
    }

    @Test
    public void deleteFood_NotFound() throws IOException, InterruptedException {
        HttpResponse<String> response = httpTestUtil.sendRequest("DELETE", "/api/food/nonexistentid", null);
        Assertions.assertEquals(404, response.statusCode(), "Expected Status Code 404");
    }

    @Test
    public void deleteFood_Unauthorized_NoToken() throws IOException, InterruptedException {
        String foodId = createTestFood();

        // Create HttpTestUtil instance without auth token
        HttpTestUtil noAuthHttpTestUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, API_KEY);
        HttpResponse<String> response = noAuthHttpTestUtil.sendRequest("DELETE", "/api/food/" + foodId, null);
        
        Assertions.assertEquals(401, response.statusCode(), "Expected Status Code 401");

        // Verify food still exists
        HttpResponse<String> getResponse = httpTestUtil.sendRequest("GET", "/api/food/" + foodId, null);
        Assertions.assertEquals(200, getResponse.statusCode(), "Food should still exist");
    }

    @Test
    public void deleteFood_Unauthorized_NoApiKey() throws IOException, InterruptedException {
        String foodId = createTestFood();

        // Create HttpTestUtil instance without API key
        HttpTestUtil noApiKeyHttpTestUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, null, authToken);
        HttpResponse<String> response = noApiKeyHttpTestUtil.sendRequest("DELETE", "/api/food/" + foodId, null);
        
        Assertions.assertEquals(401, response.statusCode(), "Expected Status Code 401");
    }

    @Test
    public void deleteFood_DifferentUser() throws IOException, InterruptedException {
        String foodId = createTestFood();

        // Register and login as different user
        String registerJson = """
                {
                    "username": "testuser2",
                    "email": "test2@example.com",
                    "password": "Password123!"
                }""";

        httpTestUtil.sendRequest("POST", "/api/auth/register", registerJson);

        String loginJson = """
                {
                    "email": "test2@example.com",
                    "password": "Password123!"
                }""";

        HttpResponse<String> loginResponse = httpTestUtil.sendRequest("POST", "/api/auth/login", loginJson);
        String differentUserToken = objectMapper.readTree(loginResponse.body()).get("token").asText();

        // Create HttpTestUtil instance with different user's token
        HttpTestUtil differentUserHttpTestUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, API_KEY, differentUserToken);
        HttpResponse<String> response = differentUserHttpTestUtil.sendRequest("DELETE", "/api/food/" + foodId, null);
        
        Assertions.assertEquals(403, response.statusCode(), "Expected Status Code 403");
    }
}