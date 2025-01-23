package com.microservice.user_service.IntegrationTests.FoodIntegrationTests;

import com.microservice.user_service.IntegrationTests.BaseIntegrationTest;
import com.microservice.user_service.model.Food;
import com.microservice.user_service.repository.FoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class UpdateFoodIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
    void updateFood_Success() throws Exception {
        Food existingFood = createTestFood("Original Meal", LocalDate.now());

        Food updatedFood = new Food();
        updatedFood.setName("Updated Meal");
        updatedFood.setProtein(50.0);
        updatedFood.setCarb(60.0);
        updatedFood.setFat(25.0);
        updatedFood.setDate(LocalDate.now());

        mockMvc.perform(put("/api/food/" + existingFood.getId())
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedFood)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Meal"))
                .andExpect(jsonPath("$.protein").value(50.0))
                .andExpect(jsonPath("$.carb").value(60.0))
                .andExpect(jsonPath("$.fat").value(25.0));
    }

    @Test
    void updateFood_NotFound() throws Exception {
        Food updatedFood = new Food();
        updatedFood.setName("Updated Meal");
        updatedFood.setProtein(50.0);
        updatedFood.setCarb(60.0);
        updatedFood.setFat(25.0);
        updatedFood.setDate(LocalDate.now());

        mockMvc.perform(put("/api/food/nonexistentid")
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedFood)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateFood_InvalidData() throws Exception {
        Food existingFood = createTestFood("Original Meal", LocalDate.now());

        Food updatedFood = new Food();
        updatedFood.setProtein(-50.0); // Invalid negative value

        mockMvc.perform(put("/api/food/" + existingFood.getId())
                .header("Authorization", "Bearer " + authToken)
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedFood)))
                .andExpect(status().isBadRequest());
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