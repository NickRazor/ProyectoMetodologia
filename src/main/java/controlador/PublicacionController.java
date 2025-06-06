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
import java.util.Date;
import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@RequestMapping("/api")
public class PublicacionController {

    private final PublicacionServicio publicacionServicio;
    private static final Logger logger = LoggerFactory.getLogger(PublicacionController.class);

    @Autowired
    public PublicacionController(PublicacionServicio publicacionServicio) {
        this.publicacionServicio = publicacionServicio;
    }

    @GetMapping("/subir")
    public String mostrarFormularioSubida() {
        return "subir"; 
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Publicacion> publicaciones = publicacionServicio.obtenerPublicaciones();
        model.addAttribute("publicaciones", publicaciones);
        return "index";
    }

    @PostMapping("/publicar")
    public String publicarProducto(
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("precio") double precio,
            @RequestParam("categoria") String categoria,
            @RequestParam("archivo") MultipartFile archivo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Object usuarioIdObj = session.getAttribute("usuarioId");
            if (usuarioIdObj == null) {
                return "redirect:/auth/login";
            }

            ObjectId usuarioId = new ObjectId(usuarioIdObj.toString());
            
            // Crear una nueva publicación
            Publicacion nuevaPublicacion = new Publicacion();
            nuevaPublicacion.setTitulo(titulo);
            nuevaPublicacion.setDescripcion(descripcion);
            nuevaPublicacion.setPrecio(precio);
            nuevaPublicacion.setCategoria(categoria);
            nuevaPublicacion.setUsuarioId(usuarioId);
            nuevaPublicacion.setFecha(new Date());
            nuevaPublicacion.setActivo(true);
            
            // Guardar la publicación y la imagen
            ObjectId publicacionId = publicacionServicio.guardarPublicacion(nuevaPublicacion, archivo);
            
            if (publicacionId != null) {
                redirectAttributes.addFlashAttribute("mensaje", "Producto publicado exitosamente");
                redirectAttributes.addFlashAttribute("tipoMensaje", "success");
                return "redirect:/user/mis-publicaciones";
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Error al publicar el producto");
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                return "redirect:/user/nueva-publicacion";
            }
        } catch (Exception e) {
            logger.error("Error al publicar producto", e);
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/user/nueva-publicacion";
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

    @GetMapping("/publicaciones/destacados")
    @ResponseBody
    public ResponseEntity<?> obtenerPublicacionesDestacadas(
            @RequestParam(value = "limite", defaultValue = "6") int limite) {
            
        logger.info("===> Recibida solicitud GET en /api/publicaciones/destacados con límite: {}", limite);
            
        try {
            List<Publicacion> destacados = publicacionServicio.obtenerPublicacionesDestacadas(limite);
            
            logger.info("Publicaciones destacadas encontradas: {}", 
                destacados != null ? destacados.size() : "null");
                
            if (destacados == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error al obtener publicaciones destacadas"));
            }
            
            return ResponseEntity.ok(destacados);
        } catch (Exception e) {
            logger.error("Error al procesar solicitud de publicaciones destacadas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener publicaciones destacadas",
                             "mensaje", e.getMessage()));
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

    @GetMapping("/publicaciones/por-publicacion-id/{publicacionId}")
    public ResponseEntity<Publicacion> obtenerPorPublicacionId(@PathVariable String publicacionId) {
        try {
            Publicacion publicacion = publicacionServicio.buscarPorPublicacionId(publicacionId);
            if (publicacion != null) {
                return ResponseEntity.ok(publicacion);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error al buscar publicación por ID amigable: {}", publicacionId, e);
            return ResponseEntity.status(500).build();
        }
    }

}
