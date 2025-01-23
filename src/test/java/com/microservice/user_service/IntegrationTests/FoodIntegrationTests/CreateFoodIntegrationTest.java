package com.microservice.user_service.IntegrationTests.FoodIntegrationTests;

import com.microservice.user_service.IntegrationTests.BaseIntegrationTest;
import com.microservice.user_service.model.Food;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class CreateFoodIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${api.key}")
    private String apiKey;

    private String authToken;

    @BeforeEach
    void setUp() {
        clearDatabase();
        authToken = getTestUtils().getAuthToken();
    }

    @Test
    void createFood_Success() throws Exception {
        Food food = new Food();
        food.setName("Test Meal");
        food.setProtein(30.0);
        food.setCarb(40.0);
        food.setFat(20.0);
        food.setDate(LocalDate.now());

        mockMvc.perform(post("/api/food")
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(food)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Meal"))
                .andExpect(jsonPath("$.protein").value(30.0))
                .andExpect(jsonPath("$.carb").value(40.0))
                .andExpect(jsonPath("$.fat").value(20.0))
                .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    void createFood_NoAuth() throws Exception {
        Food food = new Food();
        food.setName("Test Meal");

        mockMvc.perform(post("/api/food")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(food)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createFood_InvalidData() throws Exception {
        Food food = new Food();
        // Missing required fields

        mockMvc.perform(post("/api/food")
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(food)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFood_NegativeNutrients() throws Exception {
        Food food = new Food();
        food.setName("Test Meal");
        food.setProtein(-30.0);
        food.setCarb(-40.0);
        food.setFat(-20.0);
        food.setDate(LocalDate.now());

        mockMvc.perform(post("/api/food")
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(food)))
                .andExpect(status().isBadRequest());
    }
}