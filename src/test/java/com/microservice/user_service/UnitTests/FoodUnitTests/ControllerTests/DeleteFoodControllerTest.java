package com.microservice.user_service.UnitTests.FoodUnitTests.ControllerTests;

import com.microservice.user_service.controller.FoodController;
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

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeleteFoodControllerTest {

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
    void deleteFood_Success() {
        // Arrange
        doNothing().when(foodService).deleteFood(FOOD_ID, USER_ID);

        // Act
        ResponseEntity<Void> response = foodController.deleteFood(FOOD_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(foodService).deleteFood(FOOD_ID, USER_ID);
    }

    @Test
    void deleteFood_NotFound() {
        // Arrange
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found"))
            .when(foodService).deleteFood(FOOD_ID, USER_ID);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> foodController.deleteFood(FOOD_ID));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Food not found", exception.getReason());
        verify(foodService).deleteFood(FOOD_ID, USER_ID);
    }

    @Test
    void deleteFood_Unauthorized() {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class,
            () -> foodController.deleteFood(FOOD_ID));
        assertEquals("User not authenticated", exception.getMessage());
        verify(foodService, never()).deleteFood(any(), any());
    }

    @Test
    void deleteFood_Forbidden() {
        // Arrange
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"))
            .when(foodService).deleteFood(FOOD_ID, USER_ID);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> foodController.deleteFood(FOOD_ID));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Access denied", exception.getReason());
        verify(foodService).deleteFood(FOOD_ID, USER_ID);
    }

    @Test
    void deleteFood_InternalServerError() {
        // Arrange
        doThrow(new RuntimeException("Unexpected error"))
            .when(foodService).deleteFood(FOOD_ID, USER_ID);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> foodController.deleteFood(FOOD_ID));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        verify(foodService).deleteFood(FOOD_ID, USER_ID);
    }
}