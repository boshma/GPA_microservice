package com.microservice.user_service.IntegrationTests.FoodIntegrationTests;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microservice.user_service.IntegrationTests.AbstractIntegrationTest;
import com.microservice.user_service.IntegrationTests.HttpTestUtil;

public class GetFoodIntegrationTest extends AbstractIntegrationTest {
    private HttpTestUtil httpTestUtil;
    private String authToken;
    private String userId;

    @BeforeEach
    @Override
    public void setUp() throws InterruptedException, IOException {
        super.setUp();
        httpTestUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, apiKey);
        registerAndLoginUser();
        createTestFood("Test Meal 1");
        createTestFood("Test Meal 2");
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
        httpTestUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, apiKey, authToken);
    }

    private void createTestFood(String name) throws IOException, InterruptedException {
        ObjectNode foodJson = objectMapper.createObjectNode()
                .put("name", name)
                .put("protein", 30.0)
                .put("carb", 40.0)
                .put("fat", 20.0)
                .put("date", LocalDate.now().toString());

        httpTestUtil.sendRequest("POST", "/api/food", foodJson.toString());
    }

    @Test
    public void getAllFood_Success() throws IOException, InterruptedException {
        HttpResponse<String> response = httpTestUtil.sendRequest("GET", "/api/food", null);

        Assertions.assertEquals(200, response.statusCode());

        JsonNode jsonResponse = httpTestUtil.parseResponse(response);
        Assertions.assertEquals(2, jsonResponse.size());
        Assertions.assertEquals(userId, jsonResponse.get(0).get("userId").asText());
    }

    @Test
    public void getAllFood_WithDateFilter() throws IOException, InterruptedException {
        HttpResponse<String> response = httpTestUtil.sendRequest(
            "GET", 
            "/api/food?date=" + LocalDate.now().toString(), 
            null
        );

        Assertions.assertEquals(200, response.statusCode());

        JsonNode jsonResponse = httpTestUtil.parseResponse(response);
        Assertions.assertEquals(2, jsonResponse.size());
    }

    @Test
    public void getFoodById_Success() throws IOException, InterruptedException {
        // Create a food item and get its ID
        ObjectNode foodJson = objectMapper.createObjectNode()
                .put("name", "Test Meal")
                .put("protein", 30.0)
                .put("carb", 40.0)
                .put("fat", 20.0)
                .put("date", LocalDate.now().toString());

        HttpResponse<String> createResponse = httpTestUtil.sendRequest("POST", "/api/food", foodJson.toString());
        JsonNode createResponseJson = httpTestUtil.parseResponse(createResponse);
        String foodId = createResponseJson.get("id").asText();

        // Get the food item by ID
        HttpResponse<String> response = httpTestUtil.sendRequest("GET", "/api/food/" + foodId, null);

        Assertions.assertEquals(200, response.statusCode());

        JsonNode jsonResponse = httpTestUtil.parseResponse(response);
        Assertions.assertEquals("Test Meal", jsonResponse.get("name").asText());
        Assertions.assertEquals(userId, jsonResponse.get("userId").asText());
    }

    @Test
    public void getFoodById_NotFound() throws IOException, InterruptedException {
        HttpResponse<String> response = httpTestUtil.sendRequest("GET", "/api/food/nonexistentid", null);
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    public void getFoodById_Unauthorized() throws IOException, InterruptedException {
        // Create HttpTestUtil instance without auth token and API key
        HttpTestUtil noAuthHttpTestUtil = new HttpTestUtil(webClient, objectMapper, baseUrl, null, null);
        HttpResponse<String> response = noAuthHttpTestUtil.sendRequest("GET", "/api/food/someid", null);
        Assertions.assertEquals(401, response.statusCode());
    }
}