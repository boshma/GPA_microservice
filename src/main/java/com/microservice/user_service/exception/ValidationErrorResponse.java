package com.microservice.user_service.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> validationErrors;

    public ValidationErrorResponse(int status, String error, LocalDateTime timestamp, 
            Map<String, String> validationErrors) {
        super(status, error, timestamp);
        this.validationErrors = validationErrors;
    }
}