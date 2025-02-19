package mongoDB;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import modelo.Publicacion;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MongoDBCrud {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    public MongoDBCrud(@Value("${mongo.uri}") String uri, 
                       @Value("${mongo.dbName}") String dbName, 
                       @Value("${mongo.collectionName}") String collectionName) {
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

    public MongoDatabase getDatabase() {
        return this.database;
    }

    // Cerrar la conexión
    public void close() {
        mongoClient.close();
    }

    public List<Publicacion> obtenerPublicaciones() {
        List<Publicacion> publicaciones = new ArrayList<>();
        for (Document doc : collection.find()) {
            Publicacion pub = new Publicacion();
            try {
                // Convertir String a ObjectId si es necesario
                Object idObj = doc.get("_id");
                if (idObj instanceof String) {
                    pub.setId(new ObjectId((String) idObj));
                } else if (idObj instanceof ObjectId) {
                    pub.setId((ObjectId) idObj);
                }

                pub.setTitulo(doc.getString("titulo"));
                pub.setDescripcion(doc.getString("descripcion"));
                pub.setPrecio(doc.getDouble("precio"));

                // Manejar la conversión del imagenId
                Object imgIdObj = doc.get("imagenId");
                if (imgIdObj instanceof String) {
                    pub.setImagenId(new ObjectId((String) imgIdObj));
                } else if (imgIdObj instanceof ObjectId) {
                    pub.setImagenId((ObjectId) imgIdObj);
                }

                publicaciones.add(pub);
            } catch (Exception e) {
                System.err.println("Error al procesar documento: " + doc.toJson());
                e.printStackTrace();
                continue;
            }
        }
        return publicaciones;
    }

    public void guardarPublicacion(Publicacion publicacion) {
        Document doc = new Document()
            .append("titulo", publicacion.getTitulo())
            .append("descripcion", publicacion.getDescripcion())
            .append("precio", publicacion.getPrecio())
            .append("imagenId", publicacion.getImagenId())
            .append("fecha", new java.util.Date());

        collection.insertOne(doc);
    }

    public Document findByEmail(String email) {
        return collection.find(Filters.eq("email", email)).first();
    }

    public void createUser(Document userDocument) {
        collection.insertOne(userDocument);
    }
}
