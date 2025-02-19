package modelo;

import org.bson.types.ObjectId;

public class Publicacion {
    private ObjectId id;
    private String titulo;
    private String descripcion;
    private double precio;
    private ObjectId imagenId;
    private java.util.Date fecha;
    private int ratingLikes;
    private int ratingDislikes;
    // Constructor
    public Publicacion() {}

    // Getters y Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getImagenId() {
        return imagenId != null ? imagenId.toString() : null;
    }

    public void setImagenId(ObjectId imagenId) {
        this.imagenId = imagenId;
    }

    public java.util.Date getFecha() {
        return fecha;
    }

    public void setFecha(java.util.Date fecha) {
        this.fecha = fecha;
    }

    // Métodos helper para el frontend
    public String getImagenIdAsString() {
        return imagenId != null ? imagenId.toString() : null;
    }

    public String getIdAsString() {
        return id != null ? id.toString() : null;
    }
}