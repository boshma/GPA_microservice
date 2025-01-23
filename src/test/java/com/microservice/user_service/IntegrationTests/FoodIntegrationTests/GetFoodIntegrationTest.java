package com.microservice.user_service.IntegrationTests.FoodIntegrationTests;

import com.microservice.user_service.IntegrationTests.BaseIntegrationTest;
import com.microservice.user_service.model.Food;
import com.microservice.user_service.repository.FoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@AutoConfigureMockMvc
public class GetFoodIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FoodRepository foodRepository;

    @Value("${api.key}")
    private String apiKey;

    private String authToken;
    private String userId;

    @BeforeEach
    void setUp() {
        clearDatabase();
        authToken = testUtils.getAuthToken();
        userId = testUtils.getCurrentUserId();
    }

    @Test
    void getAllFood_Success() throws Exception {
        // Create test food entries
        createTestFood("Meal 1", LocalDate.now());
        createTestFood("Meal 2", LocalDate.now());

        mockMvc.perform(get("/api/food")
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", apiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].protein").exists())
                .andExpect(jsonPath("$[0].userId").value(userId));
    }

    @Test
    void getAllFood_WithDateFilter_Success() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        createTestFood("Today's Meal", today);
        createTestFood("Yesterday's Meal", yesterday);

        mockMvc.perform(get("/api/food")
                .param("date", today.toString())
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", apiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Today's Meal"));
    }

    @Test
    void getFoodById_Success() throws Exception {
        Food food = createTestFood("Test Meal", LocalDate.now());

        mockMvc.perform(get("/api/food/" + food.getId())
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", apiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Meal"))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    void getFoodById_NotFound() throws Exception {
        mockMvc.perform(get("/api/food/nonexistentid")
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", apiKey))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFoodById_Unauthorized() throws Exception {
        Food food = createTestFood("Test Meal", LocalDate.now());

        mockMvc.perform(get("/api/food/" + food.getId()))
                .andExpect(status().isUnauthorized());
    }

    private Food createTestFood(String name, LocalDate date) {
        Food food = new Food();
        food.setName(name);
        food.setProtein(30.0);
        food.setCarb(40.0);
        food.setFat(20.0);
        food.setDate(date);
        food.setUserId(userId);
        return foodRepository.save(food);
    }
}