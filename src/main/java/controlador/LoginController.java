package controlador;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import modelo.Usuario;

import servicio.UsuarioService;

import java.util.regex.Pattern;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
public class LoginController {

    private final UsuarioService usuarioService;

    public LoginController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }
    
    @PostMapping("/validar")
    public String validarUsuario(@RequestParam String email, 
                                @RequestParam String password,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            if (usuarioService.validarCredenciales(email, password)) {
                Usuario usuario = usuarioService.obtenerUsuario(email);
                // Guardar datos del usuario en la sesión
                session.setAttribute("usuarioId", usuario.getId());
                session.setAttribute("nombreUsuario", usuario.getNombre());
                session.setAttribute("email", usuario.getEmail());
                
                redirectAttributes.addFlashAttribute("mensaje", "¡Bienvenido " + usuario.getNombre() + "!");
                redirectAttributes.addFlashAttribute("tipoMensaje", "success");
                return "redirect:/user";
            } else {
                // Credenciales inválidas
                redirectAttributes.addFlashAttribute("mensaje", "Email o contraseña incorrectos");
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                return "redirect:/auth/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al iniciar sesión: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/registrar")
    public String crearUsuario(@RequestParam String nombre,
                              @RequestParam String email,
                              @RequestParam String password,
                              @RequestParam String confirmarPassword,
                              @RequestParam(required = false) boolean aceptaNotificaciones,
                              RedirectAttributes redirectAttributes) {
        try {
            // Validaciones
            if (!validarEmail(email)) {
                redirectAttributes.addFlashAttribute("mensaje", "Email inválido");
                return "redirect:/auth/login";
            }

            if (!password.equals(confirmarPassword)) {
                redirectAttributes.addFlashAttribute("mensaje", "Las contraseñas no coinciden");
                return "redirect:/auth/login";
            }

            // Intentar guardar el usuario usando el servicio
            usuarioService.guardarUsuario(nombre, email, password, aceptaNotificaciones);

            redirectAttributes.addFlashAttribute("mensaje", "Usuario registrado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            return "redirect:/auth/login";

        } catch (IllegalArgumentException e) {
            // Capturar excepciones específicas del servicio
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al registrar usuario");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private boolean validarEmail(String email) {
        String regexPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(regexPattern).matcher(email).matches();
    }
}