package modelo;

import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class CarritoRequest {
    private ObjectId productoId; // Cambiar a ObjectId
    private int cantidad;

    // Constructor por defecto necesario para Jackson
    public CarritoRequest() {}

    // Constructor con parámetros actualizado
    public CarritoRequest(String productoId, int cantidad) {
        this.productoId = new ObjectId(productoId);
        this.cantidad = cantidad;
    }

    // Getters y setters explícitos para asegurar la serialización correcta
    public ObjectId getProductoId() {
        return productoId;
    }

    public void setProductoId(ObjectId productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}