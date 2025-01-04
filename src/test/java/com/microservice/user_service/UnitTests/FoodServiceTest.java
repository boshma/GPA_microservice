package com.microservice.user_service.UnitTests;

import com.microservice.user_service.model.Food;
import com.microservice.user_service.repository.FoodRepository;
import com.microservice.user_service.service.FoodService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FoodServiceTest {

    private FoodService foodService;

    @Mock
    private FoodRepository foodRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); 
        foodService = new FoodService();
        foodService.setFoodRepository(foodRepository);
    }

    @Test
    void createFood_Success() {
        Food food = new Food();
        food.setName("Chicken");
        food.setProtein(45.5);
        food.setCarb(12.3);
        food.setFat(15.0);
        food.setDate(LocalDate.now());
        food.setUserId("user123");

        when(foodRepository.save(any(Food.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Food result = foodService.createFood(food);

        assertNotNull(result);
        assertEquals("Chicken", result.getName());
        assertEquals(45.5, result.getProtein());
        assertEquals("user123", result.getUserId());
        verify(foodRepository, times(1)).save(any(Food.class));
    }

    @Test
    void createFood_Invalid_MissingName() {
        Food food = new Food();
        food.setName("");
        food.setProtein(45.5);
        food.setCarb(12.3);
        food.setFat(15.0);
        food.setDate(LocalDate.now());
        food.setUserId("user123");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> foodService.createFood(food));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Name is required", exception.getReason());
    }

    @Test
    void createFood_Invalid_NegativeNutritionalValues() {
        Food food = new Food();
        food.setName("Chicken");
        food.setProtein(-10);
        food.setCarb(12.3);
        food.setFat(15.0);
        food.setDate(LocalDate.now());
        food.setUserId("user123");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> foodService.createFood(food));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Nutritional values cannot be negative", exception.getReason());
    }


    @Test
    void getAllFood_ByUserId_Success() {

        Food food1 = new Food("1", "Meal 1", 10, 20, 30, LocalDate.now(), "user123", LocalDateTime.now(), LocalDateTime.now());
        Food food2 = new Food("2", "Meal 2", 15, 25, 35, LocalDate.now(), "user123", LocalDateTime.now(), LocalDateTime.now());

        when(foodRepository.findByUserId("user123")).thenReturn(List.of(food1, food2));

        List<Food> result = foodService.getAllFood(null, "user123");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(foodRepository, times(1)).findByUserId("user123");
    }


    @Test
    void getAllFood_ByDateAndUserId_Success() {

        LocalDate date = LocalDate.now();
        Food food = new Food("1", "Meal 1", 10, 20, 30, date, "user123", LocalDateTime.now(), LocalDateTime.now());

        when(foodRepository.findByDateAndUserId(date, "user123")).thenReturn(List.of(food));

        List<Food> result = foodService.getAllFood(date.toString(), "user123");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(foodRepository, times(1)).findByDateAndUserId(date, "user123");
    }

    @Test
    void getFoodById_Success() {

        Food food = new Food("1", "Meal 1", 10, 20, 30, LocalDate.now(), "user123", LocalDateTime.now(), LocalDateTime.now());

        when(foodRepository.findById("1")).thenReturn(Optional.of(food));

        Food result = foodService.getFoodById("1", "user123");

        assertNotNull(result);
        assertEquals("Meal 1", result.getName());
        verify(foodRepository, times(1)).findById("1");
    }


    @Test
    void getFoodById_NotFound() {

        when(foodRepository.findById("1")).thenReturn(Optional.empty());


        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> foodService.getFoodById("1", "user123"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Meal not found", exception.getReason());
    }

    @Test
    void updateFood_Success() {

        Food existingFood = new Food("1", "Meal 1", 10, 20, 30, LocalDate.now(), "user123", LocalDateTime.now(), LocalDateTime.now());
        Food updatedFood = new Food();
        updatedFood.setName("Updated Meal");
        updatedFood.setProtein(50);
        updatedFood.setCarb(40);
        updatedFood.setFat(30);
        updatedFood.setDate(LocalDate.now());

        when(foodRepository.findById("1")).thenReturn(Optional.of(existingFood));
        when(foodRepository.save(any(Food.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Food result = foodService.updateFood("1", updatedFood, "user123");

        assertNotNull(result);
        assertEquals("Updated Meal", result.getName());
        assertEquals(50, result.getProtein());
        verify(foodRepository, times(1)).save(existingFood);
    }

    @Test
    void deleteFood_Success() {
    
        Food food = new Food("1", "Meal 1", 10, 20, 30, LocalDate.now(), "user123", LocalDateTime.now(), LocalDateTime.now());

        when(foodRepository.findById("1")).thenReturn(Optional.of(food));

  
        foodService.deleteFood("1", "user123");

        verify(foodRepository, times(1)).delete(food);
    }


    @Test
    void deleteFood_Unauthorized() {

        Food food = new Food("1", "Meal 1", 10, 20, 30, LocalDate.now(), "otherUser", LocalDateTime.now(), LocalDateTime.now());

        when(foodRepository.findById("1")).thenReturn(Optional.of(food));


        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> foodService.deleteFood("1", "user123"));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Access denied", exception.getReason());
    }
}