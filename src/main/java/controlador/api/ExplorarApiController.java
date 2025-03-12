package controlador.api;

import modelo.Publicacion;
import servicio.PublicacionServicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/explorar")
public class ExplorarApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(ExplorarApiController.class);
    
    @Autowired
    private PublicacionServicio publicacionServicio;
    
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Publicacion>> obtenerPorCategoria(@PathVariable String categoria) {
        try {
            List<Publicacion> publicaciones = publicacionServicio.obtenerPublicaciones();
            
            // Filtrar por categoría
            if (!"todos".equals(categoria)) {
                publicaciones = publicaciones.stream()
                    .filter(p -> p.getCategoria() != null && 
                                p.getCategoria().toLowerCase().equals(categoria.toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            // Limitar a máximo 20 productos
            if (publicaciones.size() > 20) {
                publicaciones = publicaciones.subList(0, 20);
            }
            
            return ResponseEntity.ok(publicaciones);
        } catch (Exception e) {
            logger.error("Error al obtener publicaciones por categoría: {}", categoria, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/random")
    public ResponseEntity<List<Publicacion>> obtenerAleatorios() {
        try {
            List<Publicacion> publicaciones = publicacionServicio.obtenerPublicacionesAleatorias(15);
            return ResponseEntity.ok(publicaciones);
        } catch (Exception e) {
            logger.error("Error al obtener publicaciones aleatorias", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}