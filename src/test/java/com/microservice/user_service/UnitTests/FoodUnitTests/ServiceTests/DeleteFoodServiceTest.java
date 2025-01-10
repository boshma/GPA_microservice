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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteFoodServiceTest {

    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private FoodService foodService;

    private static final String TEST_USER_ID = "test-user-123";

    @Test
    void deleteFood_Success() {
        Food food = createValidFood();
        food.setId("123");
        food.setUserId(TEST_USER_ID);

        when(foodRepository.findById("123")).thenReturn(Optional.of(food));
        doNothing().when(foodRepository).delete(food);

        foodService.deleteFood("123", TEST_USER_ID);

        verify(foodRepository, times(1)).findById("123");
        verify(foodRepository, times(1)).delete(food);
    }

    @Test
    void deleteFood_NotFound() {
        when(foodRepository.findById("123")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> foodService.deleteFood("123", TEST_USER_ID));
        assertEquals("Meal not found", exception.getReason());
        verify(foodRepository, never()).delete(any(Food.class));
    }

    @Test
    void deleteFood_WrongUser() {
        Food food = createValidFood();
        food.setId("123");
        food.setUserId("different-user");

        when(foodRepository.findById("123")).thenReturn(Optional.of(food));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> foodService.deleteFood("123", TEST_USER_ID));
        assertEquals("Access denied", exception.getReason());
        verify(foodRepository, never()).delete(any(Food.class));
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