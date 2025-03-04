package controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpSession; // Cambiar a javax.servlet
import modelo.Publicacion;
import servicio.PublicacionServicio;

import java.util.List;
import org.bson.types.ObjectId;

@Controller
public class UserController {
    private final PublicacionServicio publicacionServicio;

    @Autowired
    public UserController(PublicacionServicio publicacionServicio) {
        this.publicacionServicio = publicacionServicio;
    }

    @GetMapping("/user")
    public String userPage(HttpSession session, Model model) {
        try {
            Object usuarioIdObj = session.getAttribute("usuarioId");
            System.out.println("ID de usuario en sesión: " + (usuarioIdObj != null ? usuarioIdObj.toString() : "null"));
            
            if (usuarioIdObj == null) {
                return "redirect:/auth/login";
            }
            
            ObjectId usuarioId = new ObjectId(usuarioIdObj.toString());
            List<Publicacion> misPublicaciones = publicacionServicio.obtenerPublicacionesPorUsuario(usuarioId);
            
            System.out.println("Publicaciones encontradas: " + misPublicaciones.size());
            
            model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
            model.addAttribute("publicaciones", misPublicaciones);
            
            return "user";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/user/mis-publicaciones")
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
            e.printStackTrace();
            return "redirect:/auth/login";
        }
    }
}