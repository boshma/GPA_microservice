package com.microservice.user_service.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class FoodController {
    @Hidden
    @RequestMapping(value="/")
    public void redirect(HttpServletResponse response) throws IOException{
        response.sendRedirect("/swagger-ui.html");
    }


    
}
