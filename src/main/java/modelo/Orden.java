package modelo;

import org.bson.types.ObjectId;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class Orden {
    private ObjectId id;
    private ObjectId usuarioId;
    private List<CarritoItem> items;
    private double total;
    private Date fechaCompra;
    private String estado; // "PENDIENTE", "PAGADO", "ENVIADO", etc.
    private String metodoPago;
    private String direccionEnvio;
	public ObjectId getId() {
		return id;
	}
	public ObjectId getUsuarioId() {
		return usuarioId;
	}
	public List<CarritoItem> getItems() {
		return items;
	}
	public double getTotal() {
		return total;
	}
	public Date getFechaCompra() {
		return fechaCompra;
	}
	public String getEstado() {
		return estado;
	}
	public String getMetodoPago() {
		return metodoPago;
	}
	public String getDireccionEnvio() {
		return direccionEnvio;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public void setUsuarioId(ObjectId usuarioId) {
		this.usuarioId = usuarioId;
	}
	public void setItems(List<CarritoItem> items) {
		this.items = items;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	public void setFechaCompra(Date fechaCompra) {
		this.fechaCompra = fechaCompra;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public void setMetodoPago(String metodoPago) {
		this.metodoPago = metodoPago;
	}
	public void setDireccionEnvio(String direccionEnvio) {
		this.direccionEnvio = direccionEnvio;
	}
    
}