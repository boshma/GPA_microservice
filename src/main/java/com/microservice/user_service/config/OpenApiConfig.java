package com.microservice.user_service.config;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public") 
                .pathsToMatch("/**") 
                .build();
    }


    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("apiKey", new SecurityScheme()
                                .name("X-API-KEY")
                                .type(SecurityScheme.Type.APIKEY) 
                                .in(SecurityScheme.In.HEADER))) 
                .addSecurityItem(new SecurityRequirement().addList("apiKey")); 
    }
}