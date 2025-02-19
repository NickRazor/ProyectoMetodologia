package modelo;

import org.bson.types.ObjectId;
import java.util.Date;

public class Usuario {
    private ObjectId id;
    private String nombre;
    private String email;
    private String password;
    private boolean aceptaNotificaciones;
    private Date fechaRegistro;

    // Constructor vacío
    public Usuario() {}

    // Constructor con parámetros
    public Usuario(String nombre, String email, String password, boolean aceptaNotificaciones) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.aceptaNotificaciones = aceptaNotificaciones;
        this.fechaRegistro = new Date();
    }

    // Getters y setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAceptaNotificaciones() {
        return aceptaNotificaciones;
    }

    public void setAceptaNotificaciones(boolean aceptaNotificaciones) {
        this.aceptaNotificaciones = aceptaNotificaciones;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
