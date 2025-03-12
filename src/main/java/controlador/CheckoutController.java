package controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class CheckoutController {
    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);

    @GetMapping("/checkout")
    public String mostrarCheckout(HttpSession session, Model model) {
        try {
            ObjectId usuarioId = (ObjectId) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                logger.warn("Intento de acceder a checkout sin autenticación");
                return "redirect:/auth/login"; // Redirigir al login, no al carrito
            }
            
            logger.info("Usuario {} accediendo a la página de checkout", usuarioId);
            
            // Agregar un atributo al modelo para indicar que venimos del proceso de compra
            model.addAttribute("procesoCompra", true);
            
            // Los datos del carrito se manejarán desde JavaScript usando sessionStorage
            return "checkout"; // Retornar la vista checkout
        } catch (Exception e) {
            logger.error("Error al cargar la página de checkout", e);
            return "redirect:/error";
        }
    }
}