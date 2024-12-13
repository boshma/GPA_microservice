package com.microservice.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microservice.user_service.model.Food;
import com.microservice.user_service.repository.FoodRepository;

import java.util.List;

@Service
public class FoodService {

    @Autowired
    private FoodRepository foodRepository;

   

    public List<Food> getAllFood(){
        return foodRepository.findAll();
    
    }


    
}