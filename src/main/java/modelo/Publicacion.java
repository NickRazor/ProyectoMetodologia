package modelo;

import org.bson.types.ObjectId;
import java.text.SimpleDateFormat;

public class Publicacion {
    private ObjectId id;
    private String publicacionId; // Nuevo campo para ID más amigable
    private String titulo;
    private String descripcion;
    private double precio;
    private ObjectId imagenId;
    private java.util.Date fecha;
    private int ratingLikes;
    private int ratingDislikes;
    private ObjectId usuarioId;
    private String categoria; // Nueva variable
    private boolean activo; // Nuevo campo para controlar si la publicación está activa

    // Constructor actualizado
    public Publicacion() {
        this.fecha = new java.util.Date();
        this.publicacionId = generarPublicacionId();
        this.activo = true; // Por defecto, las publicaciones están activas
        this.ratingLikes = 0;
        this.ratingDislikes = 0;
    }

    // Método para generar ID único y amigable
    public String generarPublicacionId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = sdf.format(new java.util.Date());
        return "PUB-" + timeStamp + "-" + System.nanoTime() % 1000;
    }
    
    // Getters y Setters para el nuevo campo activo
    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    // Getters y Setters para el nuevo campo
    public String getPublicacionId() {
        return publicacionId;
    }

    public void setPublicacionId(String publicacionId) {
        this.publicacionId = publicacionId;
    }

    // Método para validar si una publicación pertenece a un usuario
    public boolean perteneceAUsuario(ObjectId usuarioId) {
        return this.usuarioId != null && this.usuarioId.equals(usuarioId);
    }

    // Método para verificar si la publicación está completa
    public boolean isValid() {
        return titulo != null && !titulo.trim().isEmpty() &&
               descripcion != null && !descripcion.trim().isEmpty() &&
               categoria != null && !categoria.trim().isEmpty() &&
               precio >= 0 &&
               imagenId != null;
    }

    // Getters y Setters
    public ObjectId getId() {
        return id;
    }
    public ObjectId getImagenId() {
    	return imagenId;
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

    public String getImagenIdString() {
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

    public ObjectId getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(ObjectId usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public int getRatingLikes() {
		return ratingLikes;
	}

	public int getRatingDislikes() {
		return ratingDislikes;
	}

	// Métodos helper para el frontend
    public String getImagenIdAsString() {
        return imagenId != null ? imagenId.toString() : null;
    }

    // Modificar el método getIdAsString para asegurar que devuelve un string válido
    public String getIdAsString() {
        return id != null ? id.toString() : null;
    }
    
    // Agregar un nuevo método para obtener el ID más amigable
    public String getIdentificador() {
        return this.publicacionId != null ? this.publicacionId : this.getIdAsString();
    }
    
    public void setRatingLikes(int ratingLikes) {
		this.ratingLikes = ratingLikes;
	}

	public void setRatingDislikes(int ratingDislikes) {
		this.ratingDislikes = ratingDislikes;
	}

    // Agregar getter y setter para categoria
    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
	@Override
    public String toString() {
        return "Publicacion{" +
                "publicacionId='" + publicacionId + '\'' +
                ", titulo='" + titulo + '\'' +
                ", precio=" + precio +
                ", fecha=" + fecha +
                '}';
    }
}