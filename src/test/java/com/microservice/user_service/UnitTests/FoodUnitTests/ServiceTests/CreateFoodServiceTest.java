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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateFoodServiceTest {

    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private FoodService foodService;

    @Test
    void createFood_Success() {
        Food food = createValidFood();
        when(foodRepository.save(any(Food.class))).thenReturn(food);

        Food result = foodService.createFood(food);

        assertNotNull(result);
        assertEquals("Test Food", result.getName());
        assertEquals(20.0, result.getProtein());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(foodRepository, times(1)).save(any(Food.class));
    }

    @Test
    void createFood_EmptyName() {
        Food food = createValidFood();
        food.setName("");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> foodService.createFood(food));
        assertEquals("Name is required", exception.getReason());
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void createFood_NegativeNutrients() {
        Food food = createValidFood();
        food.setProtein(-1.0);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> foodService.createFood(food));
        assertEquals("Nutritional values cannot be negative", exception.getReason());
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void createFood_NoDate() {
        Food food = createValidFood();
        food.setDate(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> foodService.createFood(food));
        assertEquals("Date is required", exception.getReason());
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