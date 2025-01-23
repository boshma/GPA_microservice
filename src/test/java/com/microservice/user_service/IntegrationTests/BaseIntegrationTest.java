package com.microservice.user_service.IntegrationTests;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected TestUtils testUtils;

    protected void clearDatabase() {
        for (String collectionName : mongoTemplate.getCollectionNames()) {
            mongoTemplate.getCollection(collectionName).drop();
        }
    }

    protected TestUtils getTestUtils() {
        return testUtils;
    }
}