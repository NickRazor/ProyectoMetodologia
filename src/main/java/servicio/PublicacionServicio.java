package servicio;

import mongoDB.MongoDBCrud;
import mongoDB.GridFSService;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.model.Filters;

import modelo.Publicacion;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

@Service
public class PublicacionServicio {
    private final MongoDBCrud mongoCrud;
    private final GridFSService gridFSService;

    public PublicacionServicio(
            @Qualifier("publicacionesMongoDBCrud") MongoDBCrud mongoCrud, 
            GridFSService gridFSService) {
        this.mongoCrud = mongoCrud;
        this.gridFSService = gridFSService;
    }

    public ObjectId guardarPublicacion(MultipartFile archivo, String titulo, String descripcion, double precio, String categoria, ObjectId usuarioId) throws IOException {
        try {
            // Guardar archivo temporalmente
            File tempFile = convertMultiPartToFile(archivo);
            
            // Redimensionar la imagen a 185x115
            File resizedFile = new File("resized_" + tempFile.getName());
            Thumbnails.of(tempFile)
                      .size(185, 115)
                      .toFile(resizedFile);
            
            // Subir archivo redimensionado a GridFS usando FileInputStream
            try (FileInputStream fis = new FileInputStream(resizedFile)) {
                ObjectId fileId = gridFSService.uploadFile(fis, resizedFile.getName());
                
                // Logging para debug
                System.out.println("Guardando publicación para usuario: " + usuarioId.toString());
                
                // Crear documento para la publicación
                Document doc = new Document()
                        .append("titulo", titulo)
                        .append("descripcion", descripcion)
                        .append("precio", precio)
                        .append("categoria", categoria) // Agregar categoría
                        .append("imagenId", fileId)
                        .append("fecha", new java.util.Date())
                        .append("usuarioId", usuarioId.toString());  // Asegurarnos de guardarlo siempre como String
                
                // Logging del documento antes de guardar
                System.out.println("Documento a guardar: " + doc.toJson());
                
                // Guardar documento en MongoDB
                ObjectId publicacionId = mongoCrud.create(doc);
                System.out.println("Publicación guardada con ID: " + publicacionId.toString());
                
                // Eliminar archivos temporales
                tempFile.delete();
                resizedFile.delete();
                
                return publicacionId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al guardar la publicación: " + e.getMessage());
        }
    }

    public List<Publicacion> obtenerPublicaciones() {
        try {
            List<Document> documentos = mongoCrud.findAllDocuments();
            List<Publicacion> publicaciones = new ArrayList<>();
            
            for (Document doc : documentos) {
                try {
                    System.out.println("Procesando documento: " + doc.toJson()); // Debug
                    
                    Publicacion pub = new Publicacion();
                    // Datos básicos
                    pub.setId(doc.getObjectId("_id"));
                    pub.setTitulo(doc.getString("titulo"));
                    pub.setDescripcion(doc.getString("descripcion"));
                    pub.setPrecio(doc.getDouble("precio"));
                    pub.setCategoria(doc.getString("categoria"));
                    pub.setFecha(doc.getDate("fecha"));
                    
                    // ID de publicación amigable
                    String publicacionId = doc.getString("publicacionId");
                    if (publicacionId == null) {
                        pub.setPublicacionId(pub.generarPublicacionId());
                    } else {
                        pub.setPublicacionId(publicacionId);
                    }
                    
                    // Ratings
                    pub.setRatingLikes(doc.getInteger("ratingLikes", 0));
                    pub.setRatingDislikes(doc.getInteger("ratingDislikes", 0));
                    
                    // Manejo de IDs de objeto
                    // ImagenId
                    Object imagenIdObj = doc.get("imagenId");
                    if (imagenIdObj instanceof String) {
                        pub.setImagenId(new ObjectId((String) imagenIdObj));
                    } else if (imagenIdObj instanceof ObjectId) {
                        pub.setImagenId((ObjectId) imagenIdObj);
                    }
                    
                    // UsuarioId
                    Object userIdObj = doc.get("usuarioId");
                    if (userIdObj instanceof String) {
                        pub.setUsuarioId(new ObjectId((String) userIdObj));
                    } else if (userIdObj instanceof ObjectId) {
                        pub.setUsuarioId((ObjectId) userIdObj);
                    }
                    
                    System.out.println("Publicación procesada: " + pub.toString()); // Debug
                    publicaciones.add(pub);
                    
                } catch (Exception e) {
                    System.err.println("Error procesando documento: " + doc.toJson());
                    e.printStackTrace();
                    // Continuar con el siguiente documento
                }
            }
            
            System.out.println("Total publicaciones procesadas: " + publicaciones.size());
            return publicaciones;
            
        } catch (Exception e) {
            System.err.println("Error al obtener publicaciones: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Publicacion> obtenerPublicacionesPorUsuario(ObjectId usuarioId) {
        try {
            System.out.println("Buscando publicaciones para usuario: " + usuarioId.toString());
            
            Document filtro = new Document("$or", List.of(
                new Document("usuarioId", usuarioId.toString()),
                new Document("usuarioId", usuarioId)
            ));
            
            List<Document> documentos = mongoCrud.findDocuments(filtro);
            System.out.println("Documentos encontrados: " + documentos.size());
            
            List<Publicacion> publicaciones = new ArrayList<>();
            
            for (Document doc : documentos) {
                try {
                    System.out.println("Procesando documento: " + doc.toJson());
                    
                    Publicacion pub = new Publicacion();
                    // Datos básicos
                    pub.setId(doc.getObjectId("_id"));
                    pub.setTitulo(doc.getString("titulo"));
                    pub.setDescripcion(doc.getString("descripcion"));
                    pub.setPrecio(doc.getDouble("precio"));
                    pub.setCategoria(doc.getString("categoria")); // Agregamos la categoría
                    pub.setFecha(doc.getDate("fecha"));
                    
                    // ID de publicación amigable
                    String publicacionId = doc.getString("publicacionId");
                    if (publicacionId == null) {
                        pub.setPublicacionId(pub.generarPublicacionId());
                    } else {
                        pub.setPublicacionId(publicacionId);
                    }
                    
                    // Ratings
                    pub.setRatingLikes(doc.getInteger("ratingLikes", 0));
                    pub.setRatingDislikes(doc.getInteger("ratingDislikes", 0));
                    
                    // Manejar el imagenId
                    Object imagenIdObj = doc.get("imagenId");
                    if (imagenIdObj instanceof String) {
                        pub.setImagenId(new ObjectId((String) imagenIdObj));
                    } else if (imagenIdObj instanceof ObjectId) {
                        pub.setImagenId((ObjectId) imagenIdObj);
                    }
                    
                    // Manejar el usuarioId
                    Object userIdObj = doc.get("usuarioId");
                    if (userIdObj instanceof String) {
                        pub.setUsuarioId(new ObjectId((String) userIdObj));
                    } else if (userIdObj instanceof ObjectId) {
                        pub.setUsuarioId((ObjectId) userIdObj);
                    }
                    
                    System.out.println("Publicación procesada: " + pub.toString());
                    publicaciones.add(pub);
                    
                } catch (Exception e) {
                    System.err.println("Error procesando documento: " + doc.toJson());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Total de publicaciones procesadas: " + publicaciones.size());
            return publicaciones;
            
        } catch (Exception e) {
            System.err.println("Error al obtener publicaciones del usuario: " + usuarioId);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @GetMapping("/imagen/{id}")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable String id) {
        try {
            byte[] imagen = gridFSService.obtenerArchivo(new ObjectId(id));
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imagen);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public boolean eliminarPublicacion(ObjectId id) {
        try {
            // Obtener la publicación para conseguir el ID de la imagen
            Publicacion publicacion = obtenerPublicacionPorId(id);
            if (publicacion != null) {
                // Verificar si hay una imagen asociada y eliminarla
                ObjectId imagenId = publicacion.getImagenId();
                if (imagenId != null) {
                    gridFSService.deleteFile(imagenId);
                }
                
                // Eliminar la publicación
                return mongoCrud.eliminarPublicacion(id);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Publicacion obtenerPublicacionPorId(ObjectId id) {
        return mongoCrud.obtenerPublicacionPorId(id);
    }

    public boolean actualizarPublicacion(ObjectId id, String titulo, String descripcion, double precio, String categoria, MultipartFile archivo) {
        try {
            Document updateDoc = new Document()
                .append("titulo", titulo)
                .append("descripcion", descripcion)
                .append("precio", precio)
                .append("categoria", categoria);  // Asegurarnos de incluir la categoría

            if (archivo != null && !archivo.isEmpty()) {
                // Guardar archivo temporalmente
                File tempFile = convertMultiPartToFile(archivo);
                
                // Redimensionar la imagen a 185x115
                File resizedFile = new File("resized_" + tempFile.getName());
                Thumbnails.of(tempFile)
                          .size(185, 115)
                          .toFile(resizedFile);
                
                // Subir archivo redimensionado a GridFS usando FileInputStream
                try (FileInputStream fis = new FileInputStream(resizedFile)) {
                    ObjectId fileId = gridFSService.uploadFile(fis, resizedFile.getName());
                    updateDoc.append("imagenId", fileId);
                }
                
                // Eliminar archivos temporales
                tempFile.delete();
                resizedFile.delete();
            }

            return mongoCrud.getCollection().updateOne(
                Filters.eq("_id", id),
                new Document("$set", updateDoc)
            ).getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();
        return convertedFile;
    }
}