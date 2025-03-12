package controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import org.bson.types.ObjectId;
import modelo.CarritoItem;
import servicio.CarritoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Controller
public class FacturaController {

    @Autowired
    private CarritoServicio carritoServicio;

    @GetMapping("/factura")
    public String mostrarFactura(HttpSession session, Model model) {
        try {
            ObjectId usuarioId = (ObjectId) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                return "redirect:/auth/login";
            }

            List<CarritoItem> items = carritoServicio.obtenerCarrito(usuarioId);
            double total = carritoServicio.calcularTotal(usuarioId);

            model.addAttribute("items", items);
            model.addAttribute("total", total);
            
            return "factura";
        } catch (Exception e) {
            return "redirect:/error";
        }
    }
}