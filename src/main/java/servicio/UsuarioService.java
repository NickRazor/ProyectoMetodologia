package servicio;

import modelo.Usuario;

import mongoDB.MongoDBCrud;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class UsuarioService {
    private final MongoDBCrud mongoCrud;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(@Qualifier("usuariosMongoDBCrud") MongoDBCrud mongoCrud) {
        this.mongoCrud = mongoCrud;
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

    public Usuario obtenerUsuarioPorId(ObjectId id) {
        try {
            Document doc = mongoCrud.read(id);
            if (doc != null) {
                Usuario usuario = new Usuario();
                usuario.setId(doc.getObjectId("_id"));
                usuario.setNombre(doc.getString("nombre"));
                usuario.setEmail(doc.getString("email"));
                usuario.setAceptaNotificaciones(doc.getBoolean("aceptaNotificaciones", false));
                return usuario;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    public boolean actualizarPassword(ObjectId id, String newPassword) {
        try {
            // Encriptar la nueva contraseña
            String hashedPassword = hashPassword(newPassword);
            
            // Crear documento con la nueva contraseña
            Document updateDoc = new Document("$set", new Document("password", hashedPassword));
            
            // Actualizar en la base de datos
            mongoCrud.update(id, updateDoc);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear la contraseña", e);
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
}