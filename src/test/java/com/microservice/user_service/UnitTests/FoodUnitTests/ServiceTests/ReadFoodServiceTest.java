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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadFoodServiceTest {

    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private FoodService foodService;

    private static final String TEST_USER_ID = "test-user-123";

    @Test
    void getAllFood_WithDate() {
        LocalDate date = LocalDate.now();
        List<Food> expectedFoods = Arrays.asList(
                createValidFood(),
                createValidFood());

        when(foodRepository.findByDateAndUserId(date, TEST_USER_ID)).thenReturn(expectedFoods);

        List<Food> result = foodService.getAllFood(date.toString(), TEST_USER_ID);

        assertEquals(2, result.size());
        verify(foodRepository, times(1)).findByDateAndUserId(date, TEST_USER_ID);
        verify(foodRepository, never()).findByUserId(TEST_USER_ID);
    }

    @Test
    void getAllFood_WithoutDate() {
        List<Food> expectedFoods = Arrays.asList(
                createValidFood(),
                createValidFood(),
                createValidFood());

        when(foodRepository.findByUserId(TEST_USER_ID)).thenReturn(expectedFoods);

        List<Food> result = foodService.getAllFood(null, TEST_USER_ID);

        assertEquals(3, result.size());
        verify(foodRepository, times(1)).findByUserId(TEST_USER_ID);
        verify(foodRepository, never()).findByDateAndUserId(any(), any());
    }

    @Test
    void getFoodById_Success() {
        Food food = createValidFood();
        food.setId("123");
        food.setUserId(TEST_USER_ID);

        when(foodRepository.findById("123")).thenReturn(Optional.of(food));

        Food result = foodService.getFoodById("123", TEST_USER_ID);

        assertNotNull(result);
        assertEquals("Test Food", result.getName());
        assertEquals(TEST_USER_ID, result.getUserId());
    }

    @Test
    void getFoodById_NotFound() {
        when(foodRepository.findById("123")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> foodService.getFoodById("123", TEST_USER_ID));
        assertEquals("Meal not found", exception.getReason());
    }

    @Test
    void getFoodById_WrongUser() {
        Food food = createValidFood();
        food.setId("123");
        food.setUserId("different-user");

        when(foodRepository.findById("123")).thenReturn(Optional.of(food));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> foodService.getFoodById("123", TEST_USER_ID));
        assertEquals("Access denied", exception.getReason());
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