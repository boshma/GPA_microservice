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
import static org.mockito.Mockito.*;

class CreateFoodControllerTest {

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
    void createFood_Success() {
        Food food = createValidFood();
        when(foodService.createFood(any(Food.class))).thenReturn(food);

        ResponseEntity<Food> response = foodController.createFood(food);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(USER_ID, response.getBody().getUserId());
        verify(foodService).createFood(any(Food.class));
    }

    @Test
    void createFood_ValidationError() {
        Food food = createValidFood();
        when(foodService.createFood(any(Food.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> foodController.createFood(food));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid input", exception.getReason());
        verify(foodService).createFood(any(Food.class));
    }

    @Test
    void createFood_Unauthorized() {
        SecurityContextHolder.clearContext();

        SecurityException exception = assertThrows(SecurityException.class,
                () -> foodController.createFood(createValidFood()));
        assertEquals("User not authenticated", exception.getMessage());
        verify(foodService, never()).createFood(any(Food.class));
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
        food.setUserId(USER_ID);
        return food;
    }
}