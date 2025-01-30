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

public class UpdateFoodIntegrationTest extends AbstractIntegrationTest {
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

    private String createTestFood(String name) throws IOException, InterruptedException {
        Food food = new Food();
        food.setName(name);
        food.setProtein(30.0);
        food.setCarb(40.0);
        food.setFat(20.0);
        food.setDate(LocalDate.now());
        food.setUserId(userId);

        HttpResponse<String> response = httpTestUtil.sendRequest("POST", "/api/food", 
            objectMapper.writeValueAsString(food));
        Food createdFood = objectMapper.readValue(response.body(), Food.class);
        return createdFood.getId();
    }

    @Test
    public void updateFood_Success() throws IOException, InterruptedException {
        String foodId = createTestFood("Original Meal");
        logger.info("Created test food with ID: {}", foodId);

        Food updatedFood = new Food();
        updatedFood.setName("Updated Meal");
        updatedFood.setProtein(50.0);
        updatedFood.setCarb(60.0);
        updatedFood.setFat(25.0);
        updatedFood.setDate(LocalDate.now());
        updatedFood.setUserId(userId);

        String requestBody = objectMapper.writeValueAsString(updatedFood);
        logger.info("Update request body: {}", requestBody);

        HttpResponse<String> response = httpTestUtil.sendRequest("PUT", "/api/food/" + foodId, requestBody);

        logger.info("Update response status: {}", response.statusCode());
        logger.info("Update response body: {}", response.body());

        Assertions.assertEquals(200, response.statusCode(), "Expected Status Code 200");

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

        HttpResponse<String> response = httpTestUtil.sendRequest("PUT", "/api/food/nonexistentid", 
            objectMapper.writeValueAsString(updatedFood));

        Assertions.assertEquals(404, response.statusCode(), "Expected Status Code 404");
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

        HttpResponse<String> response = httpTestUtil.sendRequest("PUT", "/api/food/" + foodId, 
            objectMapper.writeValueAsString(invalidFood));

        Assertions.assertEquals(400, response.statusCode(), "Expected Status Code 400");
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

        // Create HttpTestUtil instance without auth token
        HttpTestUtil noAuthHttpTestUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, API_KEY);
        HttpResponse<String> response = noAuthHttpTestUtil.sendRequest("PUT", "/api/food/" + foodId, 
            objectMapper.writeValueAsString(updatedFood));

        Assertions.assertEquals(401, response.statusCode(), "Expected Status Code 401");
    }

    @Test
    public void updateFood_DifferentUser() throws IOException, InterruptedException {
        String foodId = createTestFood("Original Meal");

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

        Food updatedFood = new Food();
        updatedFood.setName("Updated Meal");
        updatedFood.setProtein(50.0);
        updatedFood.setCarb(60.0);
        updatedFood.setFat(25.0);
        updatedFood.setDate(LocalDate.now());
        updatedFood.setUserId(userId);

        // Create HttpTestUtil instance with different user's token
        HttpTestUtil differentUserHttpTestUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, API_KEY, differentUserToken);
        HttpResponse<String> response = differentUserHttpTestUtil.sendRequest("PUT", "/api/food/" + foodId, 
            objectMapper.writeValueAsString(updatedFood));

        Assertions.assertEquals(403, response.statusCode(), "Expected Status Code 403");
    }
}