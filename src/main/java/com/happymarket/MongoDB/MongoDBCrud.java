package com.happymarket.MongoDB;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class MongoDBCrud {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    public MongoDBCrud(String uri, String dbName, String collectionName) {
        this.mongoClient = MongoClients.create(uri);
        this.database = mongoClient.getDatabase(dbName);
        this.collection = database.getCollection(collectionName);
    }

    // Crear un nuevo documento
    public ObjectId create(Document document) {
        collection.insertOne(document);
        return document.getObjectId("_id");
    }

    // Leer un documento por ID
    public Document read(ObjectId id) {
        return collection.find(Filters.eq("_id", id)).first();
    }

    // Leer todos los documentos
    public List<Document> readAll() {
        return collection.find().into(new ArrayList<>());
    }

    // Actualizar un documento por ID
    public void update(ObjectId id, Document updatedDocument) {
        collection.updateOne(Filters.eq("_id", id), new Document("$set", updatedDocument));
    }

    // Borrar un documento por ID
    public void delete(ObjectId id) {
        collection.deleteOne(Filters.eq("_id", id));
    }

    // Cerrar la conexión
    public void close() {
        mongoClient.close();
    }
}
