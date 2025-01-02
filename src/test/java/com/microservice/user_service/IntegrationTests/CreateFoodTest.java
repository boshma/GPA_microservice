package com.microservice.user_service.IntegrationTests;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;

import com.microservice.user_service.UserServiceApplication;
import com.microservice.user_service.model.Food;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class CreateFoodTest {
    ApplicationContext app;
    HttpClient webClient;
    ObjectMapper objectMapper;
    private static final String TEST_USER_ID = "test-user-123";
    private String apiKey;

    @BeforeEach
    public void setUp() throws InterruptedException {
        webClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String[] args = new String[] {};
        app = SpringApplication.run(UserServiceApplication.class, args);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.refresh();
        Environment environment = context.getEnvironment();
        apiKey = environment.getProperty("api.key");
        Thread.sleep(500);
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        Thread.sleep(500);
        SpringApplication.exit(app);
    }

    @Test
    public void createFoodSuccessful() throws IOException, InterruptedException {
        String json = "{\"name\":\"Chicken Salad\",\"protein\":45.5,\"carb\":12.3,\"fat\":15.0,\"date\":\"2024-12-17\"}";
        HttpRequest postFoodRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .header("X-User-ID", TEST_USER_ID)
                .header("X-API-KEY", apiKey) 
                .build();
        HttpResponse<String> response = webClient.send(postFoodRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(201, status, "Expected Status Code 201 - Actual Code was: " + status);
    
        Food actualResult = objectMapper.readValue(response.body(), Food.class);
        Assertions.assertEquals("Chicken Salad", actualResult.getName());
        Assertions.assertEquals(45.5, actualResult.getProtein());
        Assertions.assertEquals(TEST_USER_ID, actualResult.getUserId());
    }
    
    @Test
    public void createFoodWithoutApiKey() throws IOException, InterruptedException {
        String json = "{\"name\":\"Chicken Salad\",\"protein\":45.5,\"carb\":12.3,\"fat\":15.0,\"date\":\"2024-12-17\"}";
        HttpRequest postFoodRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .header("X-User-ID", TEST_USER_ID)
                .build();
        HttpResponse<String> response = webClient.send(postFoodRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(401, status, "Expected Status Code 401 - Actual Code was: " + status);
    }

    @Test
    public void createFoodEmptyName() throws IOException, InterruptedException {
        String json = "{\"name\":\"\",\"protein\":45.5,\"carb\":12.3,\"fat\":15.0,\"date\":\"2024-12-17\"}";
        HttpRequest postFoodRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .header("X-User-ID", TEST_USER_ID)
                .build();
        HttpResponse<String> response = webClient.send(postFoodRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    @Test
    public void createFoodNegativeNutrients() throws IOException, InterruptedException {
        String json = "{\"name\":\"Invalid Food\",\"protein\":-45.5,\"carb\":12.3,\"fat\":15.0,\"date\":\"2024-12-17\"}";
        HttpRequest postFoodRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .header("X-User-ID", TEST_USER_ID)
                .build();
        HttpResponse<String> response = webClient.send(postFoodRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    @Test
    public void createFoodWithoutUserId() throws IOException, InterruptedException {
        String json = "{\"name\":\"Chicken Salad\",\"protein\":45.5,\"carb\":12.3,\"fat\":15.0,\"date\":\"2024-12-17\"}";
        HttpRequest postFoodRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = webClient.send(postFoodRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(401, status, "Expected Status Code 401 - Actual Code was: " + status);
    }

    @Test
    public void createFoodWithEmptyUserId() throws IOException, InterruptedException {
        String json = "{\"name\":\"Chicken Salad\",\"protein\":45.5,\"carb\":12.3,\"fat\":15.0,\"date\":\"2024-12-17\"}";
        HttpRequest postFoodRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/food"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .header("X-User-ID", "")
                .build();
        HttpResponse<String> response = webClient.send(postFoodRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(401, status, "Expected Status Code 401 - Actual Code was: " + status);
    }
}