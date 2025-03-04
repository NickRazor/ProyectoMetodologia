package servicio;

import mongoDB.MongoDBCrud;
import mongoDB.GridFSService;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

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
    private static final String MONGO_URI = "mongodb+srv://rslopez6:m7gHnopaMlKxbJyb@pruebacluster.uyxx2.mongodb.net/?retryWrites=true&w=majority&appName=PruebaCluster";

    public PublicacionServicio() {
        this.mongoCrud = new MongoDBCrud(MONGO_URI, "happymarket", "publicaciones");
        this.gridFSService = new GridFSService(mongoCrud.getDatabase());
    }

    public ObjectId guardarPublicacion(MultipartFile archivo, String titulo, String descripcion, double precio, ObjectId usuarioId) throws IOException {
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
        return mongoCrud.obtenerPublicaciones();
    }

    public List<Publicacion> obtenerPublicacionesPorUsuario(ObjectId usuarioId) {
        try {
            // Añadir logging para debug
            System.out.println("Buscando publicaciones para usuario: " + usuarioId.toString());
            
            // Cambiar el filtro para buscar tanto por String como por ObjectId
            Document filtro = new Document("$or", List.of(
                new Document("usuarioId", usuarioId.toString()),
                new Document("usuarioId", usuarioId)
            ));
            
            List<Document> documentos = mongoCrud.findDocuments(filtro);
            
            // Logging para verificar documentos encontrados
            System.out.println("Documentos encontrados: " + documentos.size());
            
            List<Publicacion> publicaciones = new ArrayList<>();
            
            for (Document doc : documentos) {
                try {
                    // Logging del documento para debug
                    System.out.println("Documento encontrado: " + doc.toJson());
                    
                    Publicacion pub = new Publicacion();
                    pub.setId(doc.getObjectId("_id"));
                    pub.setTitulo(doc.getString("titulo"));
                    pub.setDescripcion(doc.getString("descripcion"));
                    pub.setPrecio(doc.getDouble("precio"));
                    
                    // Manejar el imagenId
                    Object imagenIdObj = doc.get("imagenId");
                    if (imagenIdObj instanceof String) {
                        pub.setImagenId(new ObjectId((String) imagenIdObj));
                    } else if (imagenIdObj instanceof ObjectId) {
                        pub.setImagenId((ObjectId) imagenIdObj);
                    }
                    
                    pub.setFecha(doc.getDate("fecha"));
                    
                    // Manejar el usuarioId
                    Object userIdObj = doc.get("usuarioId");
                    if (userIdObj instanceof String) {
                        pub.setUsuarioId(new ObjectId((String) userIdObj));
                    } else if (userIdObj instanceof ObjectId) {
                        pub.setUsuarioId((ObjectId) userIdObj);
                    }
                    
                    publicaciones.add(pub);
                } catch (Exception e) {
                    System.err.println("Error procesando documento: " + doc.toJson());
                    e.printStackTrace();
                }
            }
            
            // Logging final
            System.out.println("Total de publicaciones procesadas: " + publicaciones.size());
            
            return publicaciones;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al obtener publicaciones del usuario: " + usuarioId);
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

    public boolean actualizarPublicacion(ObjectId id, String titulo, String descripcion, double precio, MultipartFile archivo) {
        try {
            Publicacion publicacion = obtenerPublicacionPorId(id);
            if (publicacion == null) {
                return false;
            }

            // Actualizar los campos básicos
            publicacion.setTitulo(titulo);
            publicacion.setDescripcion(descripcion);
            publicacion.setPrecio(precio);

            // Si hay una nueva imagen, actualizarla
            if (archivo != null && !archivo.isEmpty()) {
                // Eliminar la imagen anterior si existe
                if (publicacion.getImagenId() != null) {
                    gridFSService.deleteFile(publicacion.getImagenId());
                }

                // Subir la nueva imagen
                File tempFile = convertMultiPartToFile(archivo);
                File resizedFile = new File("resized_" + tempFile.getName());
                Thumbnails.of(tempFile)
                         .size(185, 115)
                         .toFile(resizedFile);

                try (FileInputStream fis = new FileInputStream(resizedFile)) {
                    ObjectId fileId = gridFSService.uploadFile(fis, resizedFile.getName());
                    publicacion.setImagenId(fileId);
                }

                // Limpiar archivos temporales
                tempFile.delete();
                resizedFile.delete();
            }

            // Actualizar en la base de datos
            mongoCrud.actualizarPublicacion(publicacion);
            return true;
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