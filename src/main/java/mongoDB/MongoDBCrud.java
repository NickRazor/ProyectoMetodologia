package mongoDB;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import modelo.Publicacion;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

@Component
public class MongoDBCrud {
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;
    private Logger logger = LoggerFactory.getLogger(MongoDBCrud.class);
    public MongoDBCrud(@Value("${spring.data.mongodb.uri}") String uri,
                      @Value("${spring.data.mongodb.database}") String dbName,
                      @Value("${spring.data.mongodb.collection}") String collectionName) {
        try {
            System.out.println("Intentando conectar a MongoDB...");
            System.out.println("Colección a utilizar: " + collectionName);
            
            ConnectionString connectionString = new ConnectionString(uri);
            MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToSslSettings(builder -> {
                    builder.enabled(true);
                    builder.invalidHostNameAllowed(true);
                })
                .retryWrites(true)
                .build();

            this.mongoClient = MongoClients.create(settings);
            this.database = mongoClient.getDatabase(dbName);
            this.collection = database.getCollection(collectionName);
            
            database.runCommand(new Document("ping", 1));
            System.out.println("Conexión exitosa a MongoDB Atlas");
        } catch (Exception e) {
            String errorMsg = "Error al conectar con MongoDB: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public MongoCollection<Document> getCollection() {
        return collection;
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
    /**
     * Actualiza un documento en la colección de MongoDB
     * 
     * @param id ID del documento a actualizar
     * @param update Documento con los campos a actualizar
     */
    public void update(ObjectId id, Document update) {
        try {
            // Si el documento de actualización ya contiene operadores como $set, lo usamos directamente
            if (update.keySet().stream().anyMatch(key -> key.startsWith("$"))) {
                collection.updateOne(
                    Filters.eq("_id", id),
                    update
                );
            } else {
                // De lo contrario, envolvemos los campos en un operador $set
                collection.updateOne(
                    Filters.eq("_id", id),
                    new Document("$set", update)
                );
            }
            logger.info("Documento actualizado con ID: {}", id.toString());
        } catch (Exception e) {
            logger.error("Error al actualizar documento: {}", e.getMessage());
            throw e;  // Re-lanzar la excepción para manejarla en niveles superiores
        }
    }

    /**
     * Actualiza documentos que coincidan con un filtro específico
     * 
     * @param filter Filtro para los documentos a actualizar
     * @param update Operaciones de actualización a aplicar
     */
    public void updateWithFilter(Document filter, Document update) {
        try {
            logger.info("Actualizando documentos con filtro: {}", filter.toJson());
            logger.debug("Update: {}", update.toJson());
            
            collection.updateOne(filter, update);
            logger.info("Actualización completada");
        } catch (Exception e) {
            logger.error("Error al actualizar documento con filtro: ", e);
            throw e;
        }
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
            try {
                Publicacion pub = new Publicacion();
                
                // ID y campos básicos
                pub.setId(doc.getObjectId("_id"));
                pub.setTitulo(doc.getString("titulo"));
                pub.setDescripcion(doc.getString("descripcion"));
                pub.setPrecio(doc.getDouble("precio"));
                pub.setFecha(doc.getDate("fecha"));
                pub.setCategoria(doc.getString("categoria")); // Añadido categoria
                
                // PublicacionId
                String pubId = doc.getString("publicacionId");
                if (pubId == null) {
                    pubId = pub.generarPublicacionId(); // Generar si no existe
                }
                pub.setPublicacionId(pubId);

                // Manejar imagenId
                Object imgIdObj = doc.get("imagenId");
                if (imgIdObj instanceof String) {
                    pub.setImagenId(new ObjectId((String) imgIdObj));
                } else if (imgIdObj instanceof ObjectId) {
                    pub.setImagenId((ObjectId) imgIdObj);
                }

                // Usuario ID
                Object userIdObj = doc.get("usuarioId");
                if (userIdObj instanceof String) {
                    pub.setUsuarioId(new ObjectId((String) userIdObj));
                } else if (userIdObj instanceof ObjectId) {
                    pub.setUsuarioId((ObjectId) userIdObj);
                }

                // Ratings
                Integer likes = doc.getInteger("ratingLikes", 0);
                Integer dislikes = doc.getInteger("ratingDislikes", 0);
                pub.setRatingLikes(likes);
                pub.setRatingDislikes(dislikes);

                publicaciones.add(pub);
                
                // Debug
                System.out.println("Publicación procesada: " + pub.toString());
                
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
            .append("publicacionId", publicacion.getPublicacionId())
            .append("titulo", publicacion.getTitulo())
            .append("descripcion", publicacion.getDescripcion())
            .append("precio", publicacion.getPrecio())
            .append("categoria", publicacion.getCategoria())
            .append("imagenId", publicacion.getImagenId())
            .append("fecha", new java.util.Date())
            .append("usuarioId", publicacion.getUsuarioId())
            .append("ratingLikes", 0)
            .append("ratingDislikes", 0);

        collection.insertOne(doc);
    }

    public Document findByEmail(String email) {
        return collection.find(Filters.eq("email", email)).first();
    }

    public void createUser(Document userDocument) {
        collection.insertOne(userDocument);
    }

    // Agregar este nuevo método
    public List<Publicacion> find(Document filtro) {
        List<Publicacion> publicaciones = new ArrayList<>();
        for (Document doc : collection.find(filtro)) {
            Publicacion pub = new Publicacion();
            try {
                pub.setId(doc.getObjectId("_id"));
                pub.setTitulo(doc.getString("titulo"));
                pub.setDescripcion(doc.getString("descripcion"));
                pub.setPrecio(doc.getDouble("precio"));
                pub.setImagenId(doc.getObjectId("imagenId"));
                pub.setFecha(doc.getDate("fecha"));
                pub.setUsuarioId(doc.getObjectId("usuarioId"));
                
                publicaciones.add(pub);
            } catch (Exception e) {
                System.err.println("Error al procesar documento: " + doc.toJson());
                e.printStackTrace();
            }
        }
        return publicaciones;
    }

    public List<Document> findDocuments(Document filtro) {
        List<Document> documentos = new ArrayList<>();
        try {
            collection.find(filtro).into(documentos);
            return documentos;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean eliminarPublicacion(ObjectId id) {
        try {
            collection.deleteOne(Filters.eq("_id", id));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Publicacion obtenerPublicacionPorId(ObjectId id) {
        try {
            Document doc = collection.find(Filters.eq("_id", id)).first();
            if (doc != null) {
                return documentToPublicacion(doc);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Publicacion documentToPublicacion(Document doc) {
        try {
            Publicacion publicacion = new Publicacion();
            
            // Convertir campos básicos
            publicacion.setId(doc.getObjectId("_id"));
            publicacion.setTitulo(doc.getString("titulo"));
            publicacion.setDescripcion(doc.getString("descripcion"));
            publicacion.setPrecio(doc.getDouble("precio"));
            
            // Convertir ObjectId de la imagen
            Object imgIdObj = doc.get("imagenId");
            if (imgIdObj instanceof String) {
                publicacion.setImagenId(new ObjectId((String) imgIdObj));
            } else if (imgIdObj instanceof ObjectId) {
                publicacion.setImagenId((ObjectId) imgIdObj);
            }
            
            // Convertir fecha
            publicacion.setFecha(doc.getDate("fecha"));
            
            // Convertir ObjectId del usuario
            Object userIdObj = doc.get("usuarioId");
            if (userIdObj instanceof String) {
                publicacion.setUsuarioId(new ObjectId((String) userIdObj));
            } else if (userIdObj instanceof ObjectId) {
                publicacion.setUsuarioId((ObjectId) userIdObj);
            }
            
            // Convertir ratings si existen
            Integer likes = doc.getInteger("ratingLikes");
            Integer dislikes = doc.getInteger("ratingDislikes");
            if (likes != null) publicacion.setRatingLikes(likes);
            if (dislikes != null) publicacion.setRatingDislikes(dislikes);
            
            return publicacion;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean actualizarPublicacion(Publicacion publicacion) {
        try {
            Document updateDoc = new Document("$set", new Document()
                .append("titulo", publicacion.getTitulo())
                .append("descripcion", publicacion.getDescripcion())
                .append("precio", publicacion.getPrecio())
                .append("imagenId", publicacion.getImagenId())
                .append("fecha", new java.util.Date()) // Actualizar la fecha de modificación
                .append("usuarioId", publicacion.getUsuarioId().toString()));

            collection.updateOne(
                Filters.eq("_id", publicacion.getId()),
                updateDoc
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Document> findAllDocuments() {
        List<Document> documentos = new ArrayList<>();
        try {
            collection.find().into(documentos);
            return documentos;
        } catch (Exception e) {
            System.err.println("Error al obtener todos los documentos: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    public MongoCollection<Document> setCollection(String collectionName) {
        return database.getCollection(collectionName);
    }
}
