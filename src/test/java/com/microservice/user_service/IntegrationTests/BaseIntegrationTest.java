package com.microservice.user_service.IntegrationTests;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public abstract class BaseIntegrationTest {
    protected static final MongoDBContainer mongoDBContainer;
    
    static {
        mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0.8"));
        mongoDBContainer.start();
        
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        System.setProperty("spring.data.mongodb.database", "test_db");
        
        System.setProperty("api.key", "test_api_key");
        System.setProperty("jwt.secret", "test_jwt_secret_key_minimum_32_chars_long");
        System.setProperty("jwt.expiration", "3600000");
    }

    protected void clearDatabase() {
        try (MongoClient mongoClient = MongoClients.create(mongoDBContainer.getReplicaSetUrl())) {
            mongoClient.getDatabase("test_db").drop();
        }
    }
}