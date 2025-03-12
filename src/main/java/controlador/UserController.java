package controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import modelo.Publicacion;
import modelo.Usuario;
import servicio.PublicacionServicio;
import servicio.UsuarioService;
import servicio.OrdenServicio;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/user")
public class UserController {
    private final PublicacionServicio publicacionServicio;
    private final UsuarioService usuarioService;
    private final OrdenServicio ordenServicio;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    public UserController(PublicacionServicio publicacionServicio, UsuarioService usuarioService, OrdenServicio ordenServicio) {
        this.publicacionServicio = publicacionServicio;
        this.usuarioService = usuarioService;
        this.ordenServicio = ordenServicio;
    }

    @GetMapping
    public String userPage(HttpSession session, Model model) {
        try {
            Object usuarioIdObj = session.getAttribute("usuarioId");
            logger.info("ID de usuario en sesión: {}", usuarioIdObj);
            
            if (usuarioIdObj == null) {
                return "redirect:/auth/login";
            }
            
            ObjectId usuarioId = new ObjectId(usuarioIdObj.toString());
            
            // Obtener datos del usuario
            Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
            if (usuario == null) {
                logger.warn("No se pudo obtener el usuario con ID: {}", usuarioId);
                model.addAttribute("mensaje", "No se pudo cargar la información del usuario");
                model.addAttribute("tipoMensaje", "error");
                // Crear un usuario vacío para evitar errores de plantilla
                usuario = new Usuario();
                usuario.setNombre((String) session.getAttribute("nombreUsuario"));
                usuario.setEmail("No disponible");
            }
            
            // Obtener publicaciones del usuario
            List<Publicacion> misPublicaciones = publicacionServicio.obtenerPublicacionesPorUsuario(usuarioId);
            int cantidadPublicaciones = misPublicaciones != null ? misPublicaciones.size() : 0;
            
            // Crear lista de actividades recientes
            List<Map<String, Object>> actividades = new ArrayList<>();
            
            // Agregar actividades de publicaciones recientes
            if (misPublicaciones != null && !misPublicaciones.isEmpty()) {
                int limit = Math.min(misPublicaciones.size(), 5);
                for (int i = 0; i < limit; i++) {
                    Publicacion pub = misPublicaciones.get(i);
                    Map<String, Object> actividad = new HashMap<>();
                    actividad.put("tipo", "publicacion");
                    actividad.put("descripcion", "Publicaste: " + pub.getTitulo());
                    actividad.put("fecha", pub.getFecha());
                    actividades.add(actividad);
                }
            }
            
            // Agregar datos al modelo
            model.addAttribute("usuario", usuario);
            model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
            model.addAttribute("cantidadPublicaciones", cantidadPublicaciones);
            model.addAttribute("cantidadCompras", 0); // Por ahora es 0
            model.addAttribute("actividades", actividades);
            
            return "perfil";
        } catch (Exception e) {
            logger.error("Error al cargar perfil de usuario", e);
            model.addAttribute("mensaje", "Error al cargar datos del perfil: " + e.getMessage());
            model.addAttribute("tipoMensaje", "error");
            return "perfil";
        }
    }

    @GetMapping("/nueva-publicacion")
    public String nuevaPublicacion(HttpSession session, Model model) {
        // Verificar autenticación
        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/auth/login";
        }
        
        // Agregar un mensaje de depuración
        logger.info("Cargando página de nueva publicación");
        
        // Cambiamos a usar el nombre actualizado de la plantilla
        return "nueva-publicacion";
    }
    
    @GetMapping("/mis-publicaciones")
    public String misPublicaciones(HttpSession session, Model model) {
        try {
            Object usuarioIdObj = session.getAttribute("usuarioId");
            if (usuarioIdObj == null) {
                return "redirect:/auth/login";
            }
            
            ObjectId usuarioId = new ObjectId(usuarioIdObj.toString());
            List<Publicacion> misPublicaciones = publicacionServicio.obtenerPublicacionesPorUsuario(usuarioId);
            
            model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
            model.addAttribute("publicaciones", misPublicaciones);
            
            return "mis-publicaciones";
        } catch (Exception e) {
            logger.error("Error al cargar publicaciones", e);
            return "redirect:/auth/login";
        }
    }
}