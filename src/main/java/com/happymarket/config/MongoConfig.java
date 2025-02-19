package com.happymarket.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {
    
    private static final String MONGO_URI = "mongodb+srv://rslopez6:m7gHnopaMlKxbJyb@pruebacluster.uyxx2.mongodb.net/?retryWrites=true&w=majority&appName=PruebaCluster";
    
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(MONGO_URI);
    }

    @Bean
    public MongoDatabase mongoDatabase(MongoClient mongoClient) {
        return mongoClient.getDatabase("happymarket");
    }
}