package controlador;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import exception.UnauthorizedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpSession;
import modelo.CarritoItem;
import modelo.CarritoRequest;
import modelo.Publicacion;
import servicio.CarritoServicio;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {
    private static final Logger logger = LoggerFactory.getLogger(CarritoController.class);
    
    @Autowired
    private CarritoServicio carritoServicio;


    // Método auxiliar para verificar autenticación
    private ObjectId verificarAutenticacion(HttpSession session) {
        ObjectId usuarioId = (ObjectId) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            logger.warn("Intento de acceso al carrito sin autenticación");
            throw new UnauthorizedException("Usuario no autenticado");
        }
        return usuarioId;
    }
    
    @PostMapping("/agregar")
    public ResponseEntity<?> agregarAlCarrito(@RequestBody CarritoRequest request, HttpSession session) {
        try {
            ObjectId usuarioId = verificarAutenticacion(session);
            ObjectId productoId = new ObjectId(request.getProductoId().toString());
            
            carritoServicio.agregarAlCarrito(usuarioId, productoId, request.getCantidad());
            
            // Obtener carrito actualizado
            List<CarritoItem> carritoActualizado = carritoServicio.obtenerCarrito(usuarioId);
            double total = carritoServicio.calcularTotal(usuarioId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Producto agregado al carrito exitosamente");
            response.put("items", carritoActualizado);
            response.put("total", total);
            
            return ResponseEntity.ok(response);
            
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("mensaje", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error al agregar al carrito: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error al agregar el producto al carrito"));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> obtenerCarrito(HttpSession session) {
        try {
            ObjectId usuarioId = verificarAutenticacion(session);
            List<CarritoItem> items = carritoServicio.obtenerCarrito(usuarioId);
            double total = carritoServicio.calcularTotal(usuarioId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("items", items);
            response.put("total", total);
            
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error al obtener el carrito: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error interno del servidor"));
        }
    }
    
    @PutMapping("/actualizar/{itemId}")
    public ResponseEntity<?> actualizarCantidad(
            @PathVariable String itemId,
            @RequestBody Map<String, Integer> request) {
        try {
            // Validar el ID
            ObjectId id;
            try {
                id = new ObjectId(itemId);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                    .body("ID de item inválido");
            }

            // Validar la cantidad
            Integer nuevaCantidad = request.get("cantidad");
            if (nuevaCantidad == null || nuevaCantidad < 1) {
                return ResponseEntity.badRequest()
                    .body("La cantidad debe ser un número positivo");
            }

            // Actualizar cantidad
            carritoServicio.actualizarCantidad(id, nuevaCantidad);

            // Obtener carrito actualizado
            List<CarritoItem> carritoActualizado = carritoServicio.obtenerCarrito(null);
            double total = carritoServicio.calcularTotal(null);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Cantidad actualizada correctamente");
            response.put("items", carritoActualizado);
            response.put("total", total);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body("Error al actualizar cantidad: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar cantidad: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> eliminarDelCarrito(@PathVariable String itemId) {
        try {
            ObjectId id = new ObjectId(itemId);
            carritoServicio.eliminarDelCarrito(id);

            // Obtener el carrito actualizado después de eliminar
            List<CarritoItem> carritoActualizado = carritoServicio.obtenerCarrito(null);
            double total = carritoServicio.calcularTotal(null);

            // Preparar respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Item eliminado del carrito");
            response.put("items", carritoActualizado);
            response.put("total", total);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body("ID de item inválido: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar item del carrito: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al eliminar del carrito: " + e.getMessage());
        }
    }

    @GetMapping("/productos-relacionados")
    public ResponseEntity<?> obtenerProductosRelacionados(HttpSession session) {
        try {
            ObjectId usuarioId = verificarAutenticacion(session);
            List<Publicacion> productosRelacionados = carritoServicio.obtenerProductosRelacionados(usuarioId);
            
            List<Map<String, Object>> productosDTO = productosRelacionados.stream()
                .map(producto -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", producto.getId());
                    dto.put("titulo", producto.getTitulo());
                    dto.put("precio", producto.getPrecio());
                    // Convertir ObjectId a String para el imagenId
                    if (producto.getImagenId() != null) {
                        dto.put("imagenId", producto.getImagenId().toString());
                    }
                    dto.put("categoria", producto.getCategoria());
                    
                    logger.debug("Producto procesado: {}", dto); // Agregar log
                    return dto;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(productosDTO);
        } catch (Exception e) {
            logger.error("Error al obtener productos relacionados: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error al obtener productos relacionados"));
        }
    }
}