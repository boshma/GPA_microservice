package com.microservice.user_service.UnitTests.FoodUnitTests.ControllerTests;

import com.microservice.user_service.controller.FoodController;
import com.microservice.user_service.model.Food;
import com.microservice.user_service.service.FoodService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GetFoodControllerTest {

    @Mock
    private FoodService foodService;

    @InjectMocks
    private FoodController foodController;

    private final String USER_ID = "testUserId";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        Authentication auth = new UsernamePasswordAuthenticationToken(USER_ID, null, new ArrayList<>());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAllFood_Success() {
        String date = "2024-01-13";
        List<Food> expectedFoods = Arrays.asList(createValidFood(), createValidFood());
        when(foodService.getAllFood(eq(date), eq(USER_ID))).thenReturn(expectedFoods);

        ResponseEntity<List<Food>> response = foodController.getAllFood(date);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedFoods, response.getBody());
        assertEquals(2, response.getBody().size());
        verify(foodService).getAllFood(eq(date), eq(USER_ID));
    }

    @Test
    void getAllFood_NoDateProvided_Success() {
        List<Food> expectedFoods = Arrays.asList(createValidFood(), createValidFood());
        when(foodService.getAllFood(eq(null), eq(USER_ID))).thenReturn(expectedFoods);

        ResponseEntity<List<Food>> response = foodController.getAllFood(null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedFoods, response.getBody());
        verify(foodService).getAllFood(null, USER_ID);
    }

    @Test
    void getAllFood_Unauthorized() {
        SecurityContextHolder.clearContext();

        SecurityException exception = assertThrows(SecurityException.class,
            () -> foodController.getAllFood(null));
        assertEquals("User not authenticated", exception.getMessage());
        verify(foodService, never()).getAllFood(any(), any());
    }

    @Test
    void getFoodById_Success() {
        Food expectedFood = createValidFood();
        String foodId = "testFoodId";
        when(foodService.getFoodById(eq(foodId), eq(USER_ID))).thenReturn(expectedFood);

        ResponseEntity<Food> response = foodController.getFoodById(foodId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedFood, response.getBody());
        verify(foodService).getFoodById(foodId, USER_ID);
    }

    @Test
    void getFoodById_NotFound() {
        String foodId = "nonexistentId";
        when(foodService.getFoodById(eq(foodId), eq(USER_ID)))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> foodController.getFoodById(foodId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Food not found", exception.getReason());
        verify(foodService).getFoodById(foodId, USER_ID);
    }

    @Test
    void getFoodById_Unauthorized() {
        SecurityContextHolder.clearContext();

        SecurityException exception = assertThrows(SecurityException.class,
            () -> foodController.getFoodById("testId"));
        assertEquals("User not authenticated", exception.getMessage());
        verify(foodService, never()).getFoodById(any(), any());
    }

    private Food createValidFood() {
        Food food = new Food();
        food.setId("testFoodId");
        food.setName("Test Food");
        food.setProtein(20.0);
        food.setCarb(30.0);
        food.setFat(10.0);
        food.setDate(LocalDate.now());
        food.setCreatedAt(LocalDateTime.now());
        food.setUpdatedAt(LocalDateTime.now());
        food.setUserId(USER_ID);
        return food;
    }
}