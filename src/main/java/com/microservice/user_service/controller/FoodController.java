package com.microservice.user_service.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.microservice.user_service.model.Food;
import com.microservice.user_service.service.FoodService;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/food")
public class FoodController {

    @Autowired
    private FoodService foodService;

    @Hidden
    @RequestMapping(value="/")
    public void redirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("/swagger-ui.html");
    }

    @PostMapping
    public ResponseEntity<Food> createFood(@RequestBody Food food) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        food.setUserId(userId);
        Food createdFood = foodService.createFood(food);
        return ResponseEntity.status(201).body(createdFood);
    }
    //TODO
    @GetMapping
    public ResponseEntity<List<Food>> getAllFood(
            @RequestParam(required = false) String date) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Food> foods = foodService.getAllFood(date, userId); 
        return ResponseEntity.ok(foods);
    }
    //TODO
    @GetMapping("/{id}")
    public ResponseEntity<Food> getFoodById(@PathVariable String id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Food food = foodService.getFoodById(id, userId); 
        return ResponseEntity.ok(food);
    }
    //TODO
    @PutMapping("/{id}")
    public ResponseEntity<Food> updateFood(
            @PathVariable String id, 
            @RequestBody Food food) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Food updatedFood = foodService.updateFood(id, food, userId); 
        return ResponseEntity.ok(updatedFood);
    }
    //TODO
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable String id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        foodService.deleteFood(id, userId); 
        return ResponseEntity.noContent().build();
    }
}