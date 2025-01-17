package com.microservice.user_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "Food")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Food {

    @Id
    private String id; 

    private String name; 

    private double protein; 
    private double carb;  
    private double fat;   

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date; 

    private String userId; 

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAt; 

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime updatedAt;
}