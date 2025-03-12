package controlador;

import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import mongoDB.GridFSService;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ImagenController {
    
    private final GridFSService gridFSService;

    public ImagenController(GridFSService gridFSService) {
        this.gridFSService = gridFSService;
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getImagen(@PathVariable("id") String id) {
        try {
            // Verifica si el ID es válido
            if (id == null || id.isEmpty() || "[object Object]".equals(id)) {
                return ResponseEntity.badRequest().body("ID de imagen no válido");
            }
            
            // Convierte el String a ObjectId
            ObjectId objectId;
            try {
                objectId = new ObjectId(id);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Formato de ID no válido");
            }
            
            // Obtén y devuelve la imagen...
            byte[] imagen = gridFSService.obtenerArchivo(objectId);
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imagen);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error al procesar la solicitud: " + e.getMessage());
        }
    }
}