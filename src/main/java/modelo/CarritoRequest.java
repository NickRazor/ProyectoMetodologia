package modelo;

import lombok.Data;

@Data
public class CarritoRequest {
    private String productoId;
    private int cantidad;

    // Constructor por defecto necesario para Jackson
    public CarritoRequest() {}

    // Constructor con parámetros
    public CarritoRequest(String productoId, int cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    // Getters y setters explícitos para asegurar la serialización correcta
    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}