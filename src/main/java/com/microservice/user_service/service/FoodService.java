package com.microservice.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.microservice.user_service.model.Food;
import com.microservice.user_service.repository.FoodRepository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
public class FoodService {

    @Autowired
    private FoodRepository foodRepository;

    public Food createFood(Food food) {
        validateFood(food);
        food.setCreatedAt(LocalDateTime.now());
        food.setUpdatedAt(LocalDateTime.now());
        return foodRepository.save(food);
    }

    public List<Food> getAllFood(String date) {
        if (date != null) {
            LocalDate parsedDate = LocalDate.parse(date);
            return foodRepository.findByDate(parsedDate);
        }
        return foodRepository.findAll();
    }

    public Food getFoodById(String id) {
        return foodRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Meal not found"));
    }

    public Food updateFood(String id, Food food) {
        validateFood(food);
        Food existingFood = getFoodById(id);
        
        existingFood.setName(food.getName());
        existingFood.setProtein(food.getProtein());
        existingFood.setCarb(food.getCarb());
        existingFood.setFat(food.getFat());
        existingFood.setDate(food.getDate());
        existingFood.setUpdatedAt(LocalDateTime.now());
        
        return foodRepository.save(existingFood);
    }

    public void deleteFood(String id) {
        Food food = getFoodById(id);
        foodRepository.delete(food);
    }

    private void validateFood(Food food) {
        if (food.getName() == null || food.getName().trim().isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Name is required");
        }
        if (food.getProtein() < 0 || food.getCarb() < 0 || food.getFat() < 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Nutritional values cannot be negative");
        }
        if (food.getDate() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Date is required");
        }
    }
}