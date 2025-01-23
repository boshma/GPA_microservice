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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class DeleteFoodIntegrationTest extends BaseIntegrationTest {

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
    void deleteFood_Success() throws Exception {
        Food food = createTestFood("Test Meal", LocalDate.now());
        String foodId = food.getId();

        assertTrue(foodRepository.findById(foodId).isPresent());

        mockMvc.perform(delete("/api/food/" + foodId)
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", apiKey))
                .andExpect(status().isNoContent());

        Optional<Food> deletedFood = foodRepository.findById(foodId);
        assertFalse(deletedFood.isPresent());
    }

    @Test
    void deleteFood_NotFound() throws Exception {
        mockMvc.perform(delete("/api/food/nonexistentid")
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", apiKey))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteFood_Unauthorized_NoToken() throws Exception {
        Food food = createTestFood("Test Meal", LocalDate.now());

        mockMvc.perform(delete("/api/food/" + food.getId())
                .header("X-API-Key", apiKey))
                .andExpect(status().isUnauthorized());

        assertTrue(foodRepository.findById(food.getId()).isPresent());
    }

    @Test
    void deleteFood_Unauthorized_NoApiKey() throws Exception {
        Food food = createTestFood("Test Meal", LocalDate.now());

        mockMvc.perform(delete("/api/food/" + food.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isUnauthorized());

        assertTrue(foodRepository.findById(food.getId()).isPresent());
    }

    @Test
    void deleteFood_DifferentUser() throws Exception {
        Food food = createTestFood("Test Meal", LocalDate.now());
        
        testUtils.clearCurrentUser();
        String differentUserToken = testUtils.getAuthToken();


        mockMvc.perform(delete("/api/food/" + food.getId())
                .header("Authorization", "Bearer " + differentUserToken)
                .header("X-API-Key", apiKey))
                .andExpect(status().isForbidden());

        assertTrue(foodRepository.findById(food.getId()).isPresent());
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