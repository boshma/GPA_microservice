package com.microservice.user_service.IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import java.net.http.HttpClient;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.microservice.user_service.UserServiceApplication;

public abstract class AbstractIntegrationTest extends BaseIntegrationTest {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);
    protected String apiKey;
    protected ApplicationContext app;
    protected HttpClient webClient;
    protected ObjectMapper objectMapper;
    protected String baseUrl;

    @BeforeEach
    public void setUp() throws InterruptedException, IOException {
        clearDatabase();
        webClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();

        System.setProperty("server.port", "0");

        String[] args = new String[] {};
        app = SpringApplication.run(UserServiceApplication.class, args);
        
        // Get API key from environment
        apiKey = app.getEnvironment().getProperty("api.key");
        baseUrl = "http://localhost:" + app.getEnvironment().getProperty("local.server.port");
        logger.info("Application started on port: {}", app.getEnvironment().getProperty("local.server.port"));

        Thread.sleep(500);
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        Thread.sleep(500);
        SpringApplication.exit(app);
    }
}