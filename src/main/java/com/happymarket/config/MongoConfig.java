package com.happymarket.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import mongoDB.MongoDBCrud;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableMongoRepositories(basePackages = "com.happymarket.repository")
public class MongoConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(MongoConfig.class);
    
    @Value("${spring.data.mongodb.uri}")
    private String connectionString;
    
    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Bean
    public MongoClient mongoClient() {
        try {
            logger.info("Intentando conectar a MongoDB...");
            
            MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .applyToSslSettings(builder -> {
                    builder.enabled(true);
                    builder.invalidHostNameAllowed(true);
                })
                .serverApi(ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .strict(true)
                    .deprecationErrors(true)
                    .build())
                .build();

            MongoClient client = MongoClients.create(settings);
            // Verificar conexión
            client.getDatabase(databaseName).runCommand(new Document("ping", 1));
            logger.info("Conexión exitosa a MongoDB Atlas");
            return client;
        } catch (Exception e) {
            String error = "Error al conectar con MongoDB: " + e.getMessage();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

    @Bean
    public MongoDatabase mongoDatabase(MongoClient mongoClient) {
        return mongoClient.getDatabase(databaseName);
    }

    // Para el servicio de usuarios
    @Bean
    @Qualifier("usuariosMongoDBCrud")
    public MongoDBCrud usuariosMongoDBCrud(
            @Value("${spring.data.mongodb.uri}") String uri,
            @Value("${spring.data.mongodb.database}") String dbName) {
        return new MongoDBCrud(uri, dbName, "usuarios");
    }

    @Bean
    @Qualifier("publicacionesMongoDBCrud")
    public MongoDBCrud publicacionesMongoDBCrud(
            @Value("${spring.data.mongodb.uri}") String uri,
            @Value("${spring.data.mongodb.database}") String dbName) {
        return new MongoDBCrud(uri, dbName, "publicaciones");
    }

    @Bean
    @Primary
    @Qualifier("carritoMongoDBCrud")
    public MongoDBCrud carritoMongoDBCrud(
            @Value("${spring.data.mongodb.uri}") String uri,
            @Value("${spring.data.mongodb.database}") String dbName) {
        return new MongoDBCrud(uri, dbName, "carrito");
    }

    // Para el servicio de órdenes
    @Bean
    @Qualifier("ordenesMongoDBCrud")
    public MongoDBCrud ordenesMongoDBCrud(
            @Value("${spring.data.mongodb.uri}") String uri,
            @Value("${spring.data.mongodb.database}") String dbName) {
        logger.info("Creando bean ordenesMongoDBCrud para colección 'ordenes'");
        return new MongoDBCrud(uri, dbName, "ordenes");
    }
}