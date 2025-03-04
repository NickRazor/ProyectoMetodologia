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
            .append("fecha", new java.util.Date())
            .append("usuarioId", publicacion.getUsuarioId());  // Agregar usuarioId

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
}
