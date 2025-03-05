package controlador;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpSession;
import modelo.CarritoItem;
import modelo.CarritoRequest;
import servicio.CarritoServicio;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {
    private static final Logger logger = LoggerFactory.getLogger(CarritoController.class);
    
    @Autowired
    private CarritoServicio carritoServicio;
    
    @PostMapping("/agregar")
    public ResponseEntity<?> agregarAlCarrito(@RequestBody CarritoRequest request, HttpSession session) {
        try {
            ObjectId usuarioId = (ObjectId) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                return ResponseEntity.status(401).body("Usuario no autenticado");
            }

            // Convertir el String a ObjectId
            ObjectId productoId = new ObjectId(request.getProductoId());
            carritoServicio.agregarAlCarrito(usuarioId, productoId, request.getCantidad());
            
            // Devolver respuesta en formato JSON
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Producto agregado al carrito exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al agregar el producto al carrito");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @GetMapping
    public ResponseEntity<?> obtenerCarrito(HttpSession session) {
        try {
            ObjectId usuarioId = (ObjectId) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                logger.warn("Intento de acceso al carrito sin autenticación");
                return ResponseEntity.status(401).body("Usuario no autenticado");
            }

            List<CarritoItem> items = carritoServicio.obtenerCarrito(usuarioId);
            double total = carritoServicio.calcularTotal(usuarioId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("items", items);
            response.put("total", total);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al obtener el carrito: ", e);
            return ResponseEntity.status(500)
                .body("Error interno del servidor: " + e.getMessage());
        }
    }
    
    @PutMapping("/actualizar/{itemId}")
    public ResponseEntity<?> actualizarCantidad(@PathVariable String itemId, @RequestBody Map<String, Integer> request) {
        try {
            ObjectId id = new ObjectId(itemId);
            Integer nuevaCantidad = request.get("cantidad");
            
            if (nuevaCantidad == null || nuevaCantidad < 1) {
                return ResponseEntity.badRequest()
                    .body("La cantidad debe ser un número positivo");
            }

            // Actualizar en el servicio
            carritoServicio.actualizarCantidad(id, nuevaCantidad);

            // Obtener el carrito actualizado
            List<CarritoItem> carritoActualizado = carritoServicio.obtenerCarrito(null); // null para usuario actual
            double total = carritoServicio.calcularTotal(null);

            // Devolver respuesta con datos actualizados
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Cantidad actualizada correctamente");
            response.put("items", carritoActualizado);
            response.put("total", total);
            
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body("ID de item inválido: " + e.getMessage());
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
}