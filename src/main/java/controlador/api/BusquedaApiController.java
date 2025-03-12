package controlador.api;

import modelo.Publicacion;
import servicio.PublicacionServicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/publicaciones")
public class BusquedaApiController {

    private static final Logger logger = LoggerFactory.getLogger(BusquedaApiController.class);
    
    @Autowired
    private PublicacionServicio publicacionServicio;
    
    @GetMapping("/search")
    public ResponseEntity<List<Publicacion>> buscarPublicaciones(@RequestParam(value = "query", required = true) String query) {
        try {
            logger.info("Buscando publicaciones con query: {}", query);
            
            if (query == null || query.trim().isEmpty()) {
                logger.warn("Query vacía recibida");
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            // Obtener publicaciones que coincidan con la búsqueda
            List<Publicacion> resultados = publicacionServicio.buscarPublicaciones(query);
            logger.info("Se encontraron {} resultados para la búsqueda '{}'", resultados.size(), query);
            
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            logger.error("Error al buscar publicaciones con query: {}", query, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Publicacion> obtenerPublicacion(@PathVariable("id") String id) {
        try {
            logger.info("Obteniendo publicación con ID: {}", id);
            
            ObjectId objectId;
            try {
                objectId = new ObjectId(id);
            } catch (IllegalArgumentException e) {
                logger.error("ID de publicación inválido: {}", id);
                return ResponseEntity.badRequest().build();
            }
            
            Publicacion publicacion = publicacionServicio.obtenerPublicacionPorId(objectId);
            
            if (publicacion != null) {
                return ResponseEntity.ok(publicacion);
            } else {
                logger.warn("No se encontró publicación con ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error al obtener publicación con ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}