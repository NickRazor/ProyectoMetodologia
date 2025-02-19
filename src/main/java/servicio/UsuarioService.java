package servicio;

import modelo.Usuario;
import mongoDB.MongoDBCrud;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioService {
    private final MongoDBCrud mongoCrud;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final String MONGO_URI = "mongodb+srv://rslopez6:m7gHnopaMlKxbJyb@pruebacluster.uyxx2.mongodb.net/?retryWrites=true&w=majority&appName=PruebaCluster";

    public UsuarioService() {
        this.mongoCrud = new MongoDBCrud(MONGO_URI, "happymarket", "usuarios");
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public ObjectId guardarUsuario(String nombre, String email, String password, boolean aceptaNotificaciones) {
        try {
            // Verificar si el usuario ya existe
            if (mongoCrud.findByEmail(email) != null) {
                throw new IllegalArgumentException("El email ya está registrado");
            }

            // Crear documento para el usuario
            Document doc = new Document()
                    .append("nombre", nombre)
                    .append("email", email)
                    .append("password", passwordEncoder.encode(password))
                    .append("aceptaNotificaciones", aceptaNotificaciones)
                    .append("fechaRegistro", new java.util.Date());
            
            // Guardar documento en MongoDB
            return mongoCrud.create(doc);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al guardar usuario: " + e.getMessage());
        }
    }

    public Usuario obtenerUsuario(String email) {
        Document doc = mongoCrud.findByEmail(email);
        if (doc == null) {
            return null;
        }
        
        Usuario usuario = new Usuario();
        usuario.setId(doc.getObjectId("_id"));
        usuario.setNombre(doc.getString("nombre"));
        usuario.setEmail(doc.getString("email"));
        usuario.setPassword(doc.getString("password"));
        usuario.setAceptaNotificaciones(doc.getBoolean("aceptaNotificaciones"));
        usuario.setFechaRegistro(doc.getDate("fechaRegistro"));
        
        return usuario;
    }

    public boolean validarCredenciales(String email, String password) {
        Usuario usuario = obtenerUsuario(email);
        if (usuario == null) {
            return false;
        }
        return passwordEncoder.matches(password, usuario.getPassword());
    }

    public boolean actualizarUsuario(ObjectId id, String nombre, String email, boolean aceptaNotificaciones) {
        try {
            Document doc = new Document()
                    .append("nombre", nombre)
                    .append("email", email)
                    .append("aceptaNotificaciones", aceptaNotificaciones);

            mongoCrud.update(id, doc);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarUsuario(ObjectId id) {
        try {
            mongoCrud.delete(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean validarEmail(String email) {
        String regexPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email != null && email.matches(regexPattern);
    }
}