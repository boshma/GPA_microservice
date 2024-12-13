package com.microservice.user_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.microservice.user_service.model.Food;

public interface FoodRepository extends MongoRepository<Food, String> {
}