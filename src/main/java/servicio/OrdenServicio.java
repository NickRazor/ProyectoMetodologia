package servicio;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import modelo.Orden;
import modelo.CarritoItem;
import mongoDB.MongoDBCrud;

@Service
public class OrdenServicio {
    private final MongoDBCrud mongoCrud;
    public OrdenServicio(@Qualifier("ordenesMongoDBCrud") MongoDBCrud mongoCrud) {
        this.mongoCrud = mongoCrud;
    }

    public Orden guardarOrden(Orden orden) {
        try {
            // Crear documento para la orden
            Document ordenDoc = new Document()
                .append("usuarioId", orden.getUsuarioId())
                .append("total", orden.getTotal())
                .append("fechaCompra", orden.getFechaCompra())
                .append("estado", orden.getEstado())
                .append("metodoPago", orden.getMetodoPago())
                .append("direccionEnvio", orden.getDireccionEnvio());

            // Convertir items a documentos
            List<Document> itemsDoc = new ArrayList<>();
            for (CarritoItem item : orden.getItems()) {
                Document itemDoc = new Document()
                    .append("productoId", item.getProductoId())
                    .append("cantidad", item.getCantidad())
                    .append("precioUnitario", item.getPrecioUnitario())
                    .append("titulo", item.getTitulo())
                    .append("imagenUrl", item.getImagenUrl());
                itemsDoc.add(itemDoc);
            }
            ordenDoc.append("items", itemsDoc);

            // Insertar en la base de datos
            mongoCrud.getCollection().insertOne(ordenDoc);
            orden.setId(ordenDoc.getObjectId("_id"));

            return orden;
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar la orden: " + e.getMessage());
        }
    }

    public Orden obtenerOrden(ObjectId ordenId) {
        try {
            Document doc = mongoCrud.getCollection().find(Filters.eq("_id", ordenId)).first();
            if (doc == null) {
                return null;
            }

            return documentToOrden(doc);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener la orden: " + e.getMessage());
        }
    }

    public List<Orden> obtenerOrdenesPorUsuario(ObjectId usuarioId) {
        try {
            List<Orden> ordenes = new ArrayList<>();
            mongoCrud.getCollection()
                .find(Filters.eq("usuarioId", usuarioId))
                .sort(new Document("fechaCompra", -1)) // Ordenar por fecha descendente
                .forEach(doc -> ordenes.add(documentToOrden(doc)));
            return ordenes;
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener órdenes del usuario: " + e.getMessage());
        }
    }

    public void actualizarEstadoOrden(ObjectId ordenId, String nuevoEstado) {
        try {
            mongoCrud.getCollection().updateOne(
                Filters.eq("_id", ordenId),
                Updates.set("estado", nuevoEstado)
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar estado de la orden: " + e.getMessage());
        }
    }

    private Orden documentToOrden(Document doc) {
        Orden orden = new Orden();
        orden.setId(doc.getObjectId("_id"));
        orden.setUsuarioId(doc.getObjectId("usuarioId"));
        orden.setTotal(doc.getDouble("total"));
        orden.setFechaCompra(doc.getDate("fechaCompra"));
        orden.setEstado(doc.getString("estado"));
        orden.setMetodoPago(doc.getString("metodoPago"));
        orden.setDireccionEnvio(doc.getString("direccionEnvio"));

        // Convertir items
        List<CarritoItem> items = new ArrayList<>();
        List<Document> itemsDoc = doc.getList("items", Document.class);
        for (Document itemDoc : itemsDoc) {
            CarritoItem item = new CarritoItem();
            item.setProductoId(itemDoc.getObjectId("productoId"));
            item.setCantidad(itemDoc.getInteger("cantidad"));
            item.setPrecioUnitario(itemDoc.getDouble("precioUnitario"));
            item.setTitulo(itemDoc.getString("titulo"));
            item.setImagenUrl(itemDoc.getString("imagenUrl"));
            items.add(item);
        }
        orden.setItems(items);

        return orden;
    }
}