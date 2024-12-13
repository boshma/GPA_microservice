package com.microservice.user_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.microservice.user_service.model.Food;

@Repository
public interface FoodRepository extends MongoRepository<Food, String> {
}