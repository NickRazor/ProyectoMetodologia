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

@Service
public class PublicacionServicio {
    private final MongoDBCrud mongoCrud;
    private final GridFSService gridFSService;
    private static final String MONGO_URI = "mongodb+srv://rslopez6:m7gHnopaMlKxbJyb@pruebacluster.uyxx2.mongodb.net/?retryWrites=true&w=majority&appName=PruebaCluster";

    public PublicacionServicio() {
        this.mongoCrud = new MongoDBCrud(MONGO_URI, "happymarket", "publicaciones");
        this.gridFSService = new GridFSService(mongoCrud.getDatabase());
    }

    public ObjectId guardarPublicacion(MultipartFile archivo, String titulo, String descripcion, double precio) throws IOException {
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
            
            // Crear documento para la publicación
            Document doc = new Document()
                    .append("titulo", titulo)
                    .append("descripcion", descripcion)
                    .append("precio", precio)
                    .append("imageId", fileId)
                    .append("fecha", new java.util.Date());
            
            // Guardar documento en MongoDB
            ObjectId publicacionId = mongoCrud.create(doc);
            
            // Eliminar archivos temporales
            tempFile.delete();
            resizedFile.delete();
            
            return publicacionId;
        }
    }

    public List<Publicacion> obtenerPublicaciones() {
        return mongoCrud.obtenerPublicaciones();
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

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();
        return convertedFile;
    }
}