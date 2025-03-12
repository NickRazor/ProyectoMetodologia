package servicio;

import mongoDB.MongoDBCrud;
import mongoDB.GridFSService;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.util.stream.Collectors;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service // Agregamos la anotación @Service
public class PublicacionServicio {
    private final GridFSService gridFSService;
    private static final Logger logger = LoggerFactory.getLogger(PublicacionServicio.class);
    private final MongoDBCrud mongoCrud;

    @Autowired // Agregamos @Autowired al constructor
    public PublicacionServicio(
            @Qualifier("publicacionesMongoDBCrud") MongoDBCrud mongoCrud, 
            GridFSService gridFSService) {
        this.mongoCrud = mongoCrud;
        this.gridFSService = gridFSService;
    }
    
    private List<Publicacion> convertirDocumentosAPublicaciones(List<Document> documentos) {
        List<Publicacion> publicaciones = new ArrayList<>();
        for (Document doc : documentos) {
            Publicacion pub = convertirDocumentoAPublicacion(doc);
            if (pub != null) {
                publicaciones.add(pub);
            }
        }
        return publicaciones;
    }

    private Publicacion convertirDocumentoAPublicacion(Document doc) {
        try {
            Publicacion pub = new Publicacion();
            pub.setId(doc.getObjectId("_id"));
            pub.setTitulo(doc.getString("titulo"));
            pub.setDescripcion(doc.getString("descripcion"));
            pub.setPrecio(doc.getDouble("precio"));
            pub.setCategoria(doc.getString("categoria"));
            pub.setFecha(doc.getDate("fecha"));
            pub.setRatingLikes(doc.getInteger("ratingLikes", 0));
            pub.setRatingDislikes(doc.getInteger("ratingDislikes", 0));

            Object imagenIdObj = doc.get("imagenId");
            if (imagenIdObj instanceof String) {
                pub.setImagenId(new ObjectId((String) imagenIdObj));
            } else if (imagenIdObj instanceof ObjectId) {
                pub.setImagenId((ObjectId) imagenIdObj);
            }

            Object userIdObj = doc.get("usuarioId");
            if (userIdObj instanceof String) {
                pub.setUsuarioId(new ObjectId((String) userIdObj));
            } else if (userIdObj instanceof ObjectId) {
                pub.setUsuarioId((ObjectId) userIdObj);
            }

            return pub;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

// Añadir este método para aceptar un objeto Publicacion y un MultipartFile
public ObjectId guardarPublicacion(Publicacion publicacion, MultipartFile archivo) throws IOException {
    try {
        // Guardar archivo temporalmente
        File tempFile = convertMultiPartToFile(archivo);
        
        // Redimensionar la imagen a 185x115
        File resizedFile = new File("resized_" + tempFile.getName());
        Thumbnails.of(tempFile)
                  .size(185, 115)
                  .toFile(resizedFile);
        
        // Subir archivo redimensionado a GridFS usando FileInputStream
        ObjectId fileId;
        try (FileInputStream fis = new FileInputStream(resizedFile)) {
            fileId = gridFSService.uploadFile(fis, resizedFile.getName());
            
            // Asignar el ID de la imagen a la publicación
            publicacion.setImagenId(fileId);
        }
        
        // Logging para debug
        logger.info("Guardando publicación para usuario: {}", publicacion.getUsuarioId().toString());
        
        // Crear documento para la publicación
        Document doc = new Document()
                .append("titulo", publicacion.getTitulo())
                .append("descripcion", publicacion.getDescripcion())
                .append("precio", publicacion.getPrecio())
                .append("categoria", publicacion.getCategoria())
                .append("imagenId", fileId)
                .append("fecha", publicacion.getFecha())
                .append("usuarioId", publicacion.getUsuarioId().toString())  // Asegurarnos de guardarlo como String
                .append("activo", publicacion.isActivo())
                .append("ratingLikes", publicacion.getRatingLikes())
                .append("ratingDislikes", publicacion.getRatingDislikes())
                .append("publicacionId", publicacion.getPublicacionId());
        
        // Logging del documento antes de guardar
        logger.info("Documento a guardar: {}", doc.toJson());
        
        // Guardar documento en MongoDB
        ObjectId publicacionId = mongoCrud.create(doc);
        logger.info("Publicación guardada con ID: {}", publicacionId.toString());
        
        // Eliminar archivos temporales
        tempFile.delete();
        resizedFile.delete();
        
        return publicacionId;
    } catch (Exception e) {
        logger.error("Error al guardar la publicación: {}", e.getMessage());
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
        logger.debug("Intentando obtener imagen con ID: {}", id);
        ObjectId imageId = new ObjectId(id);
        byte[] imagen = gridFSService.obtenerArchivo(imageId);
        
        if (imagen == null || imagen.length == 0) {
            logger.warn("No se encontró la imagen con ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(imagen);
    } catch (Exception e) {
        logger.error("Error al obtener imagen {}: {}", id, e.getMessage());
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

public List<Publicacion> obtenerProductosRelacionados(List<String> categorias, int limite) {
    try {
        List<Publicacion> productosRelacionados = new ArrayList<>();
        
        if (categorias == null || categorias.isEmpty()) {
            // Si no hay categorías, obtener productos aleatorios recientes
            Document query = new Document();
            Document sort = new Document("fecha", -1);
            List<Document> documentos = mongoCrud.getCollection()
                .find(query)
                .sort(sort)
                .limit(limite)
                .into(new ArrayList<>());
                
            return convertirDocumentosAPublicaciones(documentos);
        }

        // Para cada categoría, obtener el producto más reciente
        for (String categoria : categorias) {
            Document query = new Document("categoria", categoria);
            Document sort = new Document("fecha", -1);
            
            Document doc = mongoCrud.getCollection()
                .find(query)
                .sort(sort)
                .limit(1)
                .first();
                
            if (doc != null) {
                Publicacion pub = convertirDocumentoAPublicacion(doc);
                if (pub != null) {
                    productosRelacionados.add(pub);
                }
            }
            
            // Si ya tenemos suficientes productos, salimos
            if (productosRelacionados.size() >= limite) {
                break;
            }
        }

        // Si no tenemos suficientes productos, completar con otros recientes
        if (productosRelacionados.size() < limite) {
            Document query = new Document("categoria", 
                new Document("$nin", categorias));
            Document sort = new Document("fecha", -1);
            
            List<Document> documentos = mongoCrud.getCollection()
                .find(query)
                .sort(sort)
                .limit(limite - productosRelacionados.size())
                .into(new ArrayList<>());
                
            productosRelacionados.addAll(convertirDocumentosAPublicaciones(documentos));
        }

        return productosRelacionados;
    } catch (Exception e) {
        logger.error("Error al obtener productos relacionados: ", e);
        return new ArrayList<>();
    }
}

public List<Publicacion> obtenerPublicacionesRecientes(int limite) {
    try {
        // Crear consulta ordenada por fecha descendente
        Document sort = new Document("fecha", -1);
        
        // Obtener documentos
        List<Document> documentos = mongoCrud.getCollection()
            .find()
            .sort(sort)
            .limit(limite)
            .into(new ArrayList<>());
        
        // Convertir documentos a publicaciones
        return convertirDocumentosAPublicaciones(documentos);
        
    } catch (Exception e) {
        logger.error("Error al obtener publicaciones recientes: ", e);
        return new ArrayList<>();
    }
}

public List<Publicacion> obtenerPublicacionesRecientesPorCategoria(String categoria, int limite) {
    try {
        // Crear filtro por categoría
        Document filtro = new Document("categoria", categoria);
        Document sort = new Document("fecha", -1);
        
        // Obtener documentos
        List<Document> documentos = mongoCrud.getCollection()
            .find(filtro)
            .sort(sort)
            .limit(limite)
            .into(new ArrayList<>());
        
        // Convertir documentos a publicaciones
        return convertirDocumentosAPublicaciones(documentos);
        
    } catch (Exception e) {
        logger.error("Error al obtener publicaciones por categoría: ", e);
        return new ArrayList<>();
    }
}

public boolean actualizarRating(ObjectId publicacionId, String tipo) {
    try {
        String campo = tipo.equals("like") ? "ratingLikes" : "ratingDislikes";
        
        // Obtener la publicación actual
        Document doc = mongoCrud.getCollection()
            .find(Filters.eq("_id", publicacionId))
            .first();
            
        if (doc == null) return false;
        
        // Obtener el valor actual e incrementarlo
        int valorActual = doc.getInteger(campo, 0);
        
        // Actualizar el documento
        Document updateDoc = new Document("$set", 
            new Document(campo, valorActual + 1));
            
        return mongoCrud.getCollection()
            .updateOne(Filters.eq("_id", publicacionId), updateDoc)
            .getModifiedCount() > 0;
            
    } catch (Exception e) {
        logger.error("Error al actualizar rating: ", e);
        return false;
    }
}

public List<Publicacion> obtenerPublicacionesDestacadas(int limite) {
    try {
        Document sort = new Document()
            .append("ratingLikes", -1)
            .append("fecha", -1);
        
        List<Document> documentos = mongoCrud.getCollection()
            .find()
            .sort(sort)
            .limit(limite)
            .into(new ArrayList<>());
        
        logger.debug("Encontradas {} publicaciones destacadas", documentos.size());
        return convertirDocumentosAPublicaciones(documentos);
        
    } catch (Exception e) {
        logger.error("Error al obtener publicaciones destacadas: ", e);
        return new ArrayList<>();
    }
}

// Agregar este método a la clase PublicacionServicio

/**
 * Obtiene una lista de publicaciones aleatorias
 * @param cantidad Cantidad de publicaciones a obtener
 * @return Lista de publicaciones aleatorias
 */
public List<Publicacion> obtenerPublicacionesAleatorias(int cantidad) {
    try {
        // Usar la agregación de MongoDB para obtener documentos aleatorios
        List<Document> pipelines = List.of(
            new Document("$sample", new Document("size", cantidad))
        );
        
        List<Document> documentos = mongoCrud.getCollection().aggregate(pipelines).into(new ArrayList<>());
        return convertirDocumentosAPublicaciones(documentos);
    } catch (Exception e) {
        logger.error("Error al obtener publicaciones aleatorias: {}", e.getMessage());
        return new ArrayList<>();
    }
}

/**
 * Busca publicaciones que coincidan con el término de búsqueda
 * @param query Término de búsqueda
 * @return Lista de publicaciones que coinciden con la búsqueda
 */
public List<Publicacion> buscarPublicaciones(String query) {
    try {
        logger.info("Buscando publicaciones que coincidan con: {}", query);
        String queryLower = query.toLowerCase();
        
        // Obtener todas las publicaciones (en producción, usar un índice o una consulta de MongoDB más eficiente)
        List<Publicacion> todasPublicaciones = obtenerPublicaciones();
        
        // Filtrar por término de búsqueda (case insensitive)
        List<Publicacion> resultados = todasPublicaciones.stream()
            .filter(p -> 
                (p.getTitulo() != null && p.getTitulo().toLowerCase().contains(queryLower)) ||
                (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(queryLower)) ||
                (p.getCategoria() != null && p.getCategoria().toLowerCase().contains(queryLower))
            )
            .limit(20) // Limitar a 20 resultados
            .collect(Collectors.toList());
            
        logger.info("Se encontraron {} resultados para la búsqueda: {}", resultados.size(), query);
        return resultados;
    } catch (Exception e) {
        logger.error("Error al buscar publicaciones: {}", e.getMessage(), e);
        return new ArrayList<>();
    }
}

/**
 * Busca una publicación por su publicacionId (ID amigable)
 * @param publicacionId El ID amigable de la publicación (formato PUB-YYYYMMDDHHMMSS-XXX)
 * @return La publicación encontrada o null si no existe
 */
public Publicacion buscarPorPublicacionId(String publicacionId) {
    try {
        if (publicacionId == null || publicacionId.isEmpty()) {
            logger.warn("ID amigable recibido está vacío");
            return null;
        }
        
        logger.info("Buscando publicación por ID amigable: {}", publicacionId);
        
        // Crear un documento de filtro por publicacionId
        Document filtro = new Document("publicacionId", publicacionId);
        
        // Buscar el documento en MongoDB
        Document doc = mongoCrud.getCollection().find(filtro).first();
        
        if (doc == null) {
            logger.warn("No se encontró publicación con ID amigable: {}", publicacionId);
            return null;
        }
        
        // Convertir documento a objeto Publicacion
        Publicacion publicacion = convertirDocumentoAPublicacion(doc);
        if (publicacion != null) {
            logger.info("Publicación encontrada con ID amigable {}: {}", publicacionId, publicacion.getId());
        }
        
        return publicacion;
    } catch (Exception e) {
        logger.error("Error al buscar publicación por ID amigable {}: {}", publicacionId, e.getMessage(), e);
        return null;
    }
}
}