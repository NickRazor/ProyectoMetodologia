package servicio;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import modelo.CarritoItem;
import modelo.Orden;
import modelo.Publicacion;
import mongoDB.MongoDBCrud;

@Service
public class CarritoServicio {
    private final MongoDBCrud mongoCrud;
    private final PublicacionServicio publicacionServicio;

    @Autowired
    private OrdenServicio ordenServicio;

    @Autowired
    public CarritoServicio(MongoDBCrud mongoCrud, PublicacionServicio publicacionServicio) {
        this.mongoCrud = mongoCrud;
        this.publicacionServicio = publicacionServicio;
    }
    
    public void agregarAlCarrito(ObjectId usuarioId, ObjectId productoId, int cantidad) {
        try {
            // Verificar si el producto ya está en el carrito
            Document existente = mongoCrud.getCollection().find(
                Filters.and(
                    Filters.eq("usuarioId", usuarioId),
                    Filters.eq("productoId", productoId)
                )
            ).first();

            if (existente != null) {
                // Actualizar cantidad si ya existe
                int cantidadActual = existente.getInteger("cantidad");
                mongoCrud.getCollection().updateOne(
                    Filters.eq("_id", existente.getObjectId("_id")),
                    Updates.set("cantidad", cantidadActual + cantidad)
                );
            } else {
                // Obtener información del producto
                Publicacion producto = publicacionServicio.obtenerPublicacionPorId(productoId);
                
                // Crear nuevo item en el carrito
                Document nuevoItem = new Document()
                    .append("usuarioId", usuarioId)
                    .append("productoId", productoId)
                    .append("cantidad", cantidad)
                    .append("precioUnitario", producto.getPrecio())
                    .append("titulo", producto.getTitulo())
                    .append("imagenId", producto.getImagenId());
                
                mongoCrud.getCollection().insertOne(nuevoItem);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al agregar al carrito: " + e.getMessage());
        }
    }
    
    public void actualizarCantidad(ObjectId itemId, int nuevaCantidad) {
        try {
            // Validar que el item exista
            Document item = mongoCrud.getCollection().find(Filters.eq("_id", itemId)).first();
            if (item == null) {
                throw new IllegalArgumentException("Item no encontrado en el carrito");
            }

            // Actualizar la cantidad
            mongoCrud.getCollection().updateOne(
                Filters.eq("_id", itemId),
                Updates.set("cantidad", nuevaCantidad)
            );

        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar cantidad: " + e.getMessage());
        }
    }
    
    public void eliminarDelCarrito(ObjectId itemId) {
        try {
            mongoCrud.getCollection().deleteOne(Filters.eq("_id", itemId));
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar del carrito: " + e.getMessage());
        }
    }
    
    public List<CarritoItem> obtenerCarrito(ObjectId usuarioId) {
        try {
            List<CarritoItem> items = new ArrayList<>();
            FindIterable<Document> documentos;

            if (usuarioId != null) {
                documentos = mongoCrud.getCollection().find(Filters.eq("usuarioId", usuarioId));
            } else {
                documentos = mongoCrud.getCollection().find();
            }

            for (Document doc : documentos) {
                CarritoItem item = new CarritoItem();
                
                // Manejo seguro de ObjectId
                try {
                    ObjectId docId = doc.getObjectId("_id");
                    item.setId(docId);
                } catch (Exception e) {
                    // Si no es un ObjectId válido, intentar convertir de string
                    String idStr = doc.getString("_id");
                    if (idStr != null) {
                        item.setId(new ObjectId(idStr));
                    }
                }
                
                // Manejo seguro del ID del producto
                try {
                    ObjectId prodId = doc.getObjectId("productoId");
                    item.setProductoId(prodId);
                } catch (Exception e) {
                    String prodIdStr = doc.getString("productoId");
                    if (prodIdStr != null) {
                        item.setProductoId(new ObjectId(prodIdStr));
                    }
                }
                
                // Manejo seguro de valores numéricos con valores por defecto
                item.setCantidad(doc.getInteger("cantidad", 1));
                item.setPrecioUnitario(doc.getDouble("precioUnitario"));
                
                // Manejo seguro de strings con valores por defecto
                item.setTitulo(doc.getString("titulo"));
                
                // Manejo de la URL de la imagen con mejor control de errores
                try {
                    Object imagenIdObj = doc.get("imagenId");
                    String imagenUrl;
                    
                    if (imagenIdObj instanceof ObjectId) {
                        imagenUrl = "/api/imagen/" + ((ObjectId) imagenIdObj).toHexString();
                    } else if (imagenIdObj instanceof String) {
                        imagenUrl = "/api/imagen/" + imagenIdObj;
                    } else {
                        imagenUrl = "/img/no-image.jpg"; // Cambio a .jpg
                    }
                    
                    item.setImagenUrl(imagenUrl);
                } catch (Exception e) {
                    item.setImagenUrl("/img/no-image.jpg"); // Imagen por defecto en caso de error
                }
                
                items.add(item);
            }

            return items;
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener el carrito: " + e.getMessage(), e);
        }
    }

    public double calcularTotal(ObjectId usuarioId) {
        List<CarritoItem> items = obtenerCarrito(usuarioId);
        return items.stream()
            .mapToDouble(item -> item.getPrecioUnitario() * item.getCantidad())
            .sum();
    }

    public Orden procesarCompra(ObjectId usuarioId, String metodoPago, String direccionEnvio) {
        try {
            // 1. Obtener items del carrito
            List<CarritoItem> itemsCarrito = obtenerCarrito(usuarioId);
            if (itemsCarrito.isEmpty()) {
                throw new IllegalStateException("El carrito está vacío");
            }

            // 2. Calcular total
            double total = calcularTotal(usuarioId);

            // 3. Crear nueva orden
            Orden nuevaOrden = new Orden();
            nuevaOrden.setUsuarioId(usuarioId);
            nuevaOrden.setItems(itemsCarrito);
            nuevaOrden.setTotal(total);
            nuevaOrden.setFechaCompra(new Date());
            nuevaOrden.setEstado("PENDIENTE");
            nuevaOrden.setMetodoPago(metodoPago);
            nuevaOrden.setDireccionEnvio(direccionEnvio);

            // 4. Guardar la orden
            Orden ordenGuardada = ordenServicio.guardarOrden(nuevaOrden);

            // 5. Limpiar el carrito solo si la orden se guardó correctamente
            if (ordenGuardada != null && ordenGuardada.getId() != null) {
                limpiarCarrito(usuarioId);
            }

            return ordenGuardada;

        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la compra: " + e.getMessage());
        }
    }

    private void limpiarCarrito(ObjectId usuarioId) {
        try {
            mongoCrud.getCollection().deleteMany(
                Filters.eq("usuarioId", usuarioId)
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al limpiar el carrito: " + e.getMessage());
        }
    }
}