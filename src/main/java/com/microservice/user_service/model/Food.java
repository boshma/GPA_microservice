package com.microservice.user_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "FoodandExercises") 
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class Food {

    @Id
    private String id; 

    private String name;
    private int protein;
    private int carb;
    private int fat;
}