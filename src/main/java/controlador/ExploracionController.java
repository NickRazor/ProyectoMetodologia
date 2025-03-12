package controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/explorar")
public class ExploracionController {
    
    private static final Logger logger = LoggerFactory.getLogger(ExploracionController.class);
    
    @GetMapping
    public String mostrarPaginaExploracion(Model model) {
        logger.info("Accediendo a la página de exploración");
        return "explorar/explorar";
    }
}