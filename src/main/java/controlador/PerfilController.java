package controlador;

import modelo.Usuario;
import servicio.UsuarioService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class PerfilController {

    private final UsuarioService usuarioService;

    @Autowired
    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(Model model, HttpSession session) {
        // Verificar si el usuario está autenticado
        ObjectId usuarioId = (ObjectId) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/auth/login";
        }

        // Obtener información del usuario
        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
        if (usuario == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("usuario", usuario);
        return "perfil";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@RequestParam("nombre") String nombre,
                                 @RequestParam("email") String email,
                                 @RequestParam(value = "newPassword", required = false) String newPassword,
                                 @RequestParam(value = "aceptaNotificaciones", defaultValue = "false") boolean aceptaNotificaciones,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        try {
            ObjectId usuarioId = (ObjectId) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                return "redirect:/auth/login";
            }

            boolean actualizado = usuarioService.actualizarUsuario(usuarioId, nombre, email, aceptaNotificaciones);
            
            if (actualizado) {
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    usuarioService.actualizarPassword(usuarioId, newPassword);
                }
                redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado exitosamente");
                redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar el perfil");
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            }

            return "redirect:/user/perfil";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/user/perfil";
        }
    }

    @DeleteMapping("/perfil/eliminar")
    @ResponseBody
    public ResponseEntity<?> eliminarCuenta(HttpSession session) {
        try {
            ObjectId usuarioId = (ObjectId) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debe iniciar sesión");
            }

            boolean eliminado = usuarioService.eliminarUsuario(usuarioId);
            if (eliminado) {
                session.invalidate();
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar la cuenta");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}