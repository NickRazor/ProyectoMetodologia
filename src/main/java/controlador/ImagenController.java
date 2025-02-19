package controlador;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import mongoDB.GridFSService;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ImagenController {
    
    private final GridFSService gridFSService;

    @Autowired
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
}