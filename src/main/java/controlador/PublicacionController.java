package controlador;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;  // Cambiar a Controller en lugar de RestController
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import modelo.Publicacion;
import servicio.PublicacionServicio;
import mongoDB.MongoDBCrud;
import mongoDB.GridFSService;

import java.io.IOException;
import java.util.List;

@Controller  // Cambiar de @RestController a @Controller
@RequestMapping("/api")
public class PublicacionController {

    private final PublicacionServicio publicacionServicio;
    private final MongoDBCrud mongoDBCrud;
    private final GridFSService gridFSService;

    @Autowired
    public PublicacionController(PublicacionServicio publicacionServicio, MongoDBCrud mongoDBCrud, GridFSService gridFSService) {
        this.publicacionServicio = publicacionServicio;
        this.mongoDBCrud = mongoDBCrud;
        this.gridFSService = gridFSService;
    }

    @GetMapping("/subir")
    public String mostrarFormularioSubida() {
        return "user"; // Cambiar de "subir" a "user"
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Publicacion> publicaciones = publicacionServicio.obtenerPublicaciones();
        model.addAttribute("publicaciones", publicaciones);
        return "index";
    }

    @PostMapping("/publicar")  // Especificar la ruta para el POST
    public String publicar(@RequestParam("titulo") String titulo,
                          @RequestParam("descripcion") String descripcion,
                          @RequestParam("precio") double precio,
                          @RequestParam("archivo") MultipartFile archivo,
                          RedirectAttributes redirectAttributes) {
        try {
            ObjectId imagenId = gridFSService.uploadResizedImage(archivo);
            
            Publicacion publicacion = new Publicacion();
            publicacion.setTitulo(titulo);
            publicacion.setDescripcion(descripcion);
            publicacion.setPrecio(precio);
            publicacion.setImagenId(imagenId);
            
            mongoDBCrud.guardarPublicacion(publicacion);
            
            redirectAttributes.addFlashAttribute("mensaje", "¡La publicación se ha guardado exitosamente!");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            
            return "redirect:/user"; // Cambiar aquí
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al guardar la publicación: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/user"; // Cambiar aquí también
        }
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }

    @GetMapping("/publicaciones")
    @ResponseBody  // Agregar esta anotación para endpoints REST
    public List<Publicacion> obtenerPublicaciones() {
        List<Publicacion> publicaciones = publicacionServicio.obtenerPublicaciones();
        System.out.println("Publicaciones encontradas: " + publicaciones.size()); // Debug
        return publicaciones;
    }
}
