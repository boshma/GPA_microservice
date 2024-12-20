package com.microservice.user_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.microservice.user_service.model.Food;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FoodRepository extends MongoRepository<Food, String> {
    List<Food> findByDate(LocalDate date);
    List<Food> findByUserId(String userId);
    List<Food> findByDateAndUserId(LocalDate date, String userId);
}