package com.microservice.user_service.config;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class TestMongoConfig {
    private static MongoServer server;
    private static String connectionString;

    @Bean
    public MongoTemplate mongoTemplate() {
        if (server == null) {
            server = new MongoServer(new MemoryBackend());
            connectionString = server.bindAndGetConnectionString();
        }

        MongoClient mongoClient = MongoClients.create(connectionString);
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient, "test"));
    }
}