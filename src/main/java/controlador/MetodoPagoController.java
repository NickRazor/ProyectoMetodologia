package controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class MetodoPagoController {
    
    @GetMapping("/metodos-pago")
    public String mostrarMetodosPago(Model model, HttpSession session) {
        // Verificar si el usuario está autenticado
        String nombreUsuario = (String) session.getAttribute("nombreUsuario");
        if (nombreUsuario == null) {
            return "redirect:/auth/login";
        }
        
        // Aquí puedes agregar lógica para obtener los métodos de pago del usuario
        // y añadirlos al modelo
        
        return "metodopago";
    }
}
