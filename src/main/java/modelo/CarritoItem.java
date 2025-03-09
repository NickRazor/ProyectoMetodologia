package modelo;

import org.bson.types.ObjectId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
@Data
public class CarritoItem {
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;
    
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId productoId;
    
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId usuarioId;
    
    private int cantidad;
    private double precioUnitario;
    private String titulo;
    private String imagenUrl;
    
    // Constructor por defecto
    public CarritoItem() {
        this.cantidad = 1;
        this.precioUnitario = 0.0;
    }
    public ObjectId getId() {
        return id;
    }
    public void setId(ObjectId id) {
        this.id = id;
    }
    public ObjectId getProductoId() {
        return productoId;
    }
    public void setProductoId(ObjectId productoId) {
        this.productoId = productoId;
    }
    public ObjectId getUsuarioId() {
        return usuarioId;
    }
    public void setUsuarioId(ObjectId usuarioId) {
        this.usuarioId = usuarioId;
    }
    public int getCantidad() {
        return cantidad;
    }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
    public double getPrecioUnitario() {
        return precioUnitario;
    }
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public String getImagenUrl() {
        return imagenUrl;
    }
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
    
    // Getters, setters y constructor
    
}
