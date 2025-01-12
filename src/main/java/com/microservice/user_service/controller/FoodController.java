package com.microservice.user_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.microservice.user_service.model.Food;
import com.microservice.user_service.service.FoodService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/food")
@Tag(name = "Food", description = "Food management APIs")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class FoodController {

    @Autowired
    private FoodService foodService;

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        return authentication.getName();
    }

    @Operation(summary = "Create a new food entry")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Food entry created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<Food> createFood(
            @RequestBody @Validated Food food) {
        food.setUserId(getCurrentUserId());
        Food createdFood = foodService.createFood(food);
        return ResponseEntity.status(201).body(createdFood);
    }

    @Operation(summary = "Get all food entries", description = "Retrieve all food entries for the authenticated user, optionally filtered by date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved food entries"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "400", description = "Invalid date format")
    })
    @GetMapping
    public ResponseEntity<List<Food>> getAllFood(
            @Parameter(description = "Date in yyyy-MM-dd format")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String date) {
        List<Food> foods = foodService.getAllFood(date, getCurrentUserId());
        return ResponseEntity.ok(foods);
    }

    @Operation(summary = "Get food entry by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved food entry"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Food entry not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Food> getFoodById(
            @Parameter(description = "Food entry ID")
            @PathVariable String id) {
        Food food = foodService.getFoodById(id, getCurrentUserId());
        return ResponseEntity.ok(food);
    }

    @Operation(summary = "Update food entry")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Food entry updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Food entry not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Food> updateFood(
            @Parameter(description = "Food entry ID")
            @PathVariable String id,
            @RequestBody @Validated Food food) {
        Food updatedFood = foodService.updateFood(id, food, getCurrentUserId());
        return ResponseEntity.ok(updatedFood);
    }

    @Operation(summary = "Delete food entry")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Food entry deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Food entry not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(
            @Parameter(description = "Food entry ID")
            @PathVariable String id) {
        foodService.deleteFood(id, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}