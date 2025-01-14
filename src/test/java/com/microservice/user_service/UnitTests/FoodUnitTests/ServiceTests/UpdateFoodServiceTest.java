package com.microservice.user_service.UnitTests.FoodUnitTests.ServiceTests;

import com.microservice.user_service.model.Food;
import com.microservice.user_service.repository.FoodRepository;
import com.microservice.user_service.service.FoodService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateFoodServiceTest {

    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private FoodService foodService;

    private static final String TEST_USER_ID = "test-user-123";

    @Test
    void updateFood_Success() {
        Food existingFood = createValidFood();
        existingFood.setId("123");
        existingFood.setUserId(TEST_USER_ID);

        Food updatedFood = createValidFood();
        updatedFood.setName("Updated Food");
        updatedFood.setProtein(30.0);
        updatedFood.setCarb(40.0);
        updatedFood.setFat(15.0);

        when(foodRepository.findById("123")).thenReturn(Optional.of(existingFood));
        when(foodRepository.save(any(Food.class))).thenReturn(updatedFood);

        Food result = foodService.updateFood("123", updatedFood, TEST_USER_ID);

        assertNotNull(result);
        assertEquals("Updated Food", result.getName());
        assertEquals(30.0, result.getProtein());
        assertEquals(40.0, result.getCarb());
        assertEquals(15.0, result.getFat());
        verify(foodRepository, times(1)).save(any(Food.class));
    }

    @Test
    void updateFood_NotFound() {
        Food updatedFood = createValidFood();
        when(foodRepository.findById("123")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> foodService.updateFood("123", updatedFood, TEST_USER_ID));
        assertEquals("Meal not found", exception.getReason());
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void updateFood_WrongUser() {
        Food existingFood = createValidFood();
        existingFood.setId("123");
        existingFood.setUserId("different-user");

        Food updatedFood = createValidFood();
        when(foodRepository.findById("123")).thenReturn(Optional.of(existingFood));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> foodService.updateFood("123", updatedFood, TEST_USER_ID));
        assertEquals("Access denied", exception.getReason());
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void updateFood_InvalidData_EmptyName() {
        Food updatedFood = createValidFood();
        updatedFood.setName("");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> foodService.updateFood("123", updatedFood, TEST_USER_ID));
        assertEquals("Name is required", exception.getReason());
        verify(foodRepository, never()).findById(any());
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void updateFood_InvalidData_NegativeNutrients() {
        Food updatedFood = createValidFood();
        updatedFood.setProtein(-1.0);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> foodService.updateFood("123", updatedFood, TEST_USER_ID));
        assertEquals("Nutritional values cannot be negative", exception.getReason());
        verify(foodRepository, never()).findById(any());
        verify(foodRepository, never()).save(any(Food.class));
    }

    private Food createValidFood() {
        Food food = new Food();
        food.setName("Test Food");
        food.setProtein(20.0);
        food.setCarb(30.0);
        food.setFat(10.0);
        food.setDate(LocalDate.now());
        food.setCreatedAt(LocalDateTime.now());
        food.setUpdatedAt(LocalDateTime.now());
        return food;
    }
}