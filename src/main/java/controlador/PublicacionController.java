package controlador;

import org.bson.types.ObjectId;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import modelo.Publicacion;
import servicio.PublicacionServicio;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/publicaciones")
public class PublicacionController {

    private final PublicacionServicio publicacionServicio;
    private static final Logger logger = LoggerFactory.getLogger(PublicacionController.class);

    @Autowired
    public PublicacionController(PublicacionServicio publicacionServicio) {
        this.publicacionServicio = publicacionServicio;
    }

    @GetMapping("/subir")
    public String mostrarFormularioSubida() {
        return "user"; 
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Publicacion> publicaciones = publicacionServicio.obtenerPublicaciones();
        model.addAttribute("publicaciones", publicaciones);
        return "index";
    }

    @PostMapping("/publicar")
    public String publicar(@RequestParam("archivo") MultipartFile archivo,
                          @RequestParam("titulo") String titulo,
                          @RequestParam("descripcion") String descripcion,
                          @RequestParam("precio") double precio,
                          @RequestParam("categoria") String categoria, // Nuevo parámetro
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        try {
            // Verificar si el usuario está autenticado
            Object usuarioIdObj = session.getAttribute("usuarioId");
            if (usuarioIdObj == null) {
                return "redirect:/auth/login";
            }

            ObjectId usuarioId = new ObjectId(usuarioIdObj.toString());
            
            // Guardar la publicación con categoría
            ObjectId publicacionId = publicacionServicio.guardarPublicacion(archivo, titulo, descripcion, precio, categoria, usuarioId);
            
            if (publicacionId != null) {
                redirectAttributes.addFlashAttribute("mensaje", "Publicación creada exitosamente");
                redirectAttributes.addFlashAttribute("tipoMensaje", "success");
                return "redirect:/user/mis-publicaciones"; // Redirige a mis publicaciones
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Error al crear la publicación");
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                return "redirect:/user"; // Redirige de vuelta al formulario
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/user";
        }
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }

  

    // Añadir método para obtener publicaciones por usuario
    @GetMapping("/mis-publicaciones")
    public String misPublicaciones(Model model, HttpSession session) {
        ObjectId usuarioId = (ObjectId) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/auth/login";
        }
        
        List<Publicacion> publicaciones = publicacionServicio.obtenerPublicacionesPorUsuario(usuarioId);
        model.addAttribute("publicaciones", publicaciones);
        return "mis-publicaciones";
    }

    @DeleteMapping("/publicaciones/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarPublicacion(@PathVariable("id") String id, HttpSession session) {
        try {
            // Verificar si el usuario está autenticado
            ObjectId usuarioId = (ObjectId) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debe iniciar sesión");
            }

            // Convertir el ID de String a ObjectId
            ObjectId publicacionId = new ObjectId(id);

            // Verificar si la publicación pertenece al usuario
            Publicacion publicacion = publicacionServicio.obtenerPublicacionPorId(publicacionId);
            if (publicacion == null) {
                return ResponseEntity.notFound().build();
            }

            if (!publicacion.getUsuarioId().equals(usuarioId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tiene permiso para eliminar esta publicación");
            }

            // Eliminar la publicación
            boolean eliminado = publicacionServicio.eliminarPublicacion(publicacionId);
            if (eliminado) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar la publicación");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/publicaciones/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable("id") String id, 
                                         Model model, 
                                         HttpSession session) {
        try {
            ObjectId publicacionId = new ObjectId(id);
            Publicacion publicacion = publicacionServicio.obtenerPublicacionPorId(publicacionId);
            
            if (publicacion != null) {
                System.out.println("Categoría en el controlador: " + publicacion.getCategoria());
                if (publicacion.getCategoria() == null || publicacion.getCategoria().isEmpty()) {
                    publicacion.setCategoria("electronics"); // Valor por defecto si es null
                }
                model.addAttribute("publicacion", publicacion);
                model.addAttribute("categorias", List.of(
                    new String[]{"electronics", "Electrónicos"},
                    new String[]{"home", "Casa y Jardín"},
                    new String[]{"toys", "Juguetes"},
                    new String[]{"watches", "Relojes"}
                ));
            }
            return "editar-publicacion";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/user/mis-publicaciones";
        }
    }

    @PostMapping("/publicaciones/{id}/editar")
    public String actualizarPublicacion(@PathVariable("id") String id,
                                      @RequestParam("titulo") String titulo,
                                      @RequestParam("descripcion") String descripcion,
                                      @RequestParam("precio") double precio,
                                      @RequestParam("categoria") String categoria, // Nuevo parámetro
                                      @RequestParam(value = "archivo", required = false) MultipartFile archivo,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        try {
            ObjectId usuarioId = (ObjectId) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                return "redirect:/auth/login";
            }

            ObjectId publicacionId = new ObjectId(id);
            Publicacion publicacion = publicacionServicio.obtenerPublicacionPorId(publicacionId);

            if (publicacion == null || !publicacion.getUsuarioId().equals(usuarioId)) {
                redirectAttributes.addFlashAttribute("mensaje", "No tiene permiso para editar esta publicación");
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                return "redirect:/user/mis-publicaciones";
            }

            boolean actualizado = publicacionServicio.actualizarPublicacion(publicacionId, titulo, descripcion, precio, categoria, archivo);
            
            if (actualizado) {
                redirectAttributes.addFlashAttribute("mensaje", "Publicación actualizada exitosamente");
                redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar la publicación");
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            }

            return "redirect:/user/mis-publicaciones";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/user/mis-publicaciones";
        }
    }


    @PostMapping("/{id}/rating/{tipo}")
    public ResponseEntity<?> actualizarRating(
            @PathVariable("id") String id,
            @PathVariable("tipo") String tipo,
            HttpSession session) {
        try {
            logger.debug("Recibiendo request para rating - ID: {}, Tipo: {}", id, tipo);

            // Verificar autenticación
            Object usuarioIdObj = session.getAttribute("usuarioId");
            if (usuarioIdObj == null) {
                logger.warn("Intento de rating sin autenticación");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Debe iniciar sesión"));
            }

            ObjectId publicacionId = new ObjectId(id);
            boolean actualizado = publicacionServicio.actualizarRating(publicacionId, tipo);

            if (actualizado) {
                Publicacion pub = publicacionServicio.obtenerPublicacionPorId(publicacionId);
                Map<String, Object> response = new HashMap<>();
                response.put("likes", pub.getRatingLikes());
                response.put("dislikes", pub.getRatingDislikes());
                response.put("mensaje", "Rating actualizado exitosamente");
                
                logger.debug("Rating actualizado - ID: {}, Likes: {}, Dislikes: {}", 
                    id, pub.getRatingLikes(), pub.getRatingDislikes());
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error al actualizar rating"));
            }

        } catch (IllegalArgumentException e) {
            logger.error("ID de publicación inválido: {}", id, e);
            return ResponseEntity.badRequest()
                .body(Map.of("mensaje", "ID de publicación inválido"));
        } catch (Exception e) {
            logger.error("Error al procesar rating: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error al procesar la solicitud"));
        }
    }

    @GetMapping("/destacados")
    public ResponseEntity<?> obtenerDestacados(
            @RequestParam(defaultValue = "6") int limite) {
        try {
            logger.debug("Obteniendo {} publicaciones destacadas", limite);
            List<Publicacion> destacados = publicacionServicio.obtenerPublicacionesDestacadas(limite);
            
            // Convertir a DTO para asegurar el formato correcto
            List<Map<String, Object>> dtos = destacados.stream()
                .map(pub -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", pub.getId());
                    dto.put("idAsString", pub.getId().toString());
                    dto.put("titulo", pub.getTitulo());
                    dto.put("descripcion", pub.getDescripcion());
                    dto.put("precio", pub.getPrecio());
                    dto.put("categoria", pub.getCategoria());
                    dto.put("fecha", pub.getFecha());
                    dto.put("imagenIdAsString", pub.getImagenId().toString());
                    dto.put("ratingLikes", pub.getRatingLikes());
                    dto.put("ratingDislikes", pub.getRatingDislikes());
                    return dto;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error al obtener publicaciones destacadas: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error al obtener productos destacados"));
        }
    }

    @GetMapping
    public ResponseEntity<?> obtenerPublicaciones() {
        try {
            List<Publicacion> publicaciones = publicacionServicio.obtenerPublicaciones();
            
            // Convertir a DTO para asegurar el formato correcto
            List<Map<String, Object>> dtos = publicaciones.stream()
                .map(pub -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", pub.getId().toString());
                    dto.put("idAsString", pub.getId().toString());
                    dto.put("titulo", pub.getTitulo());
                    dto.put("descripcion", pub.getDescripcion());
                    dto.put("precio", pub.getPrecio());
                    dto.put("categoria", pub.getCategoria());
                    dto.put("fecha", pub.getFecha());
                    dto.put("imagenIdAsString", pub.getImagenId().toString());
                    dto.put("ratingLikes", pub.getRatingLikes());
                    dto.put("ratingDislikes", pub.getRatingDislikes());
                    return dto;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error al obtener publicaciones: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error al obtener las publicaciones"));
        }
    }
}
