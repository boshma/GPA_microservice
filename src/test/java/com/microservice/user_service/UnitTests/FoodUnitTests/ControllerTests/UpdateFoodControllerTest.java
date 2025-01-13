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
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UpdateFoodControllerTest {

    @Mock
    private FoodService foodService;

    @InjectMocks
    private FoodController foodController;

    private final String USER_ID = "testUserId";
    private final String FOOD_ID = "testFoodId";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        Authentication auth = new UsernamePasswordAuthenticationToken(USER_ID, null, new ArrayList<>());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void updateFood_Success() {
        Food foodToUpdate = createValidFood();
        Food updatedFood = createValidFood();
        updatedFood.setName("Updated Food Name");
        
        when(foodService.updateFood(eq(FOOD_ID), any(Food.class), eq(USER_ID)))
            .thenReturn(updatedFood);

        ResponseEntity<Food> response = foodController.updateFood(FOOD_ID, foodToUpdate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Food Name", response.getBody().getName());
        assertEquals(USER_ID, response.getBody().getUserId());
        verify(foodService).updateFood(eq(FOOD_ID), any(Food.class), eq(USER_ID));
    }

    @Test
    void updateFood_NotFound() {
        Food foodToUpdate = createValidFood();
        when(foodService.updateFood(eq(FOOD_ID), any(Food.class), eq(USER_ID)))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> foodController.updateFood(FOOD_ID, foodToUpdate));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Food not found", exception.getReason());
        verify(foodService).updateFood(eq(FOOD_ID), any(Food.class), eq(USER_ID));
    }

    @Test
    void updateFood_ValidationError() {
        Food invalidFood = createValidFood();
        when(foodService.updateFood(eq(FOOD_ID), any(Food.class), eq(USER_ID)))
            .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> foodController.updateFood(FOOD_ID, invalidFood));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid input", exception.getReason());
        verify(foodService).updateFood(eq(FOOD_ID), any(Food.class), eq(USER_ID));
    }

    @Test
    void updateFood_Unauthorized() {
        SecurityContextHolder.clearContext();
        Food foodToUpdate = createValidFood();

        SecurityException exception = assertThrows(SecurityException.class,
            () -> foodController.updateFood(FOOD_ID, foodToUpdate));
        assertEquals("User not authenticated", exception.getMessage());
        verify(foodService, never()).updateFood(any(), any(), any());
    }

    @Test
    void updateFood_Forbidden() {
        Food foodToUpdate = createValidFood();
        when(foodService.updateFood(eq(FOOD_ID), any(Food.class), eq(USER_ID)))
            .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> foodController.updateFood(FOOD_ID, foodToUpdate));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Access denied", exception.getReason());
        verify(foodService).updateFood(eq(FOOD_ID), any(Food.class), eq(USER_ID));
    }

    private Food createValidFood() {
        Food food = new Food();
        food.setId(FOOD_ID);
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