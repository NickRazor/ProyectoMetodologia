package controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VistaCarritoController {
    
    @GetMapping("/carrito")
    public String mostrarCarrito() {
        return "carrito";
    }
}