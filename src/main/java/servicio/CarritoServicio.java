package servicio;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import modelo.CarritoItem;
import modelo.Orden;
import modelo.Publicacion;
import mongoDB.MongoDBCrud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CarritoServicio {
    private static final Logger logger = LoggerFactory.getLogger(CarritoServicio.class);
    private final MongoDBCrud mongoCrud;
    private final PublicacionServicio publicacionServicio;

    @Autowired
    private OrdenServicio ordenServicio;
    public CarritoServicio(
            @Qualifier("carritoMongoDBCrud") MongoDBCrud mongoCrud, 
            PublicacionServicio publicacionServicio) {
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
            logger.debug("Intentando actualizar cantidad: itemId={}, nuevaCantidad={}", itemId, nuevaCantidad);

            // Validar entrada
            if (itemId == null) {
                logger.error("ID del item es nulo");
                throw new IllegalArgumentException("ID del item no puede ser nulo");
            }
            if (nuevaCantidad < 1) {
                logger.error("Cantidad inválida: {}", nuevaCantidad);
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }

            // Imprimir todos los documentos en la colección para debug
            logger.debug("Contenido actual de la colección carrito:");
            mongoCrud.getCollection().find().forEach(doc -> 
                logger.debug("Item en carrito: _id={}, productoId={}, cantidad={}", 
                    doc.getObjectId("_id"), 
                    doc.getObjectId("productoId"),
                    doc.getInteger("cantidad"))
            );

            // Buscar el item
            Document item = mongoCrud.getCollection().find(Filters.eq("_id", itemId)).first();
            if (item == null) {
                logger.warn("Item no encontrado: {}. Buscando por string...", itemId);
                
                // Intentar buscar por string en caso de que el ID esté en otro formato
                item = mongoCrud.getCollection().find(
                    Filters.or(
                        Filters.eq("_id", itemId.toString()),
                        Filters.eq("_id", new ObjectId(itemId.toString()))
                    )
                ).first();
                
                if (item == null) {
                    logger.error("Item definitivamente no encontrado para ID: {}", itemId);
                    throw new IllegalArgumentException("Item no encontrado en el carrito");
                }
            }

            logger.debug("Item encontrado: {}", item.toJson());

            // Actualizar cantidad
            UpdateResult result = mongoCrud.getCollection().updateOne(
                Filters.eq("_id", item.getObjectId("_id")),
                Updates.set("cantidad", nuevaCantidad)
            );

            if (result.getModifiedCount() == 0) {
                logger.error("No se pudo actualizar la cantidad para el item: {}", itemId);
                throw new RuntimeException("No se pudo actualizar la cantidad");
            }

            logger.info("Cantidad actualizada con éxito: itemId={}, nuevaCantidad={}", itemId, nuevaCantidad);
        } catch (Exception e) {
            logger.error("Error al actualizar cantidad: {}", e.getMessage(), e);
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
    
    /**
     * Elimina un item del carrito de un usuario
     * 
     * @param usuarioId ID del usuario
     * @param itemId ID del item a eliminar
     */
    public void eliminarDelCarrito(ObjectId usuarioId, ObjectId itemId) {
        logger.info("Eliminando item {} del carrito del usuario {}", itemId, usuarioId);
        
        try {
            // Registrar el estado antes de la eliminación
            List<Document> documentosActuales = mongoCrud.getCollection().find(
                Filters.eq("usuarioId", usuarioId)
            ).into(new ArrayList<>());
            
            logger.info("Documentos asociados al usuario antes de eliminar: {}", documentosActuales.size());
            for (Document doc : documentosActuales) {
                logger.info("Documento encontrado: id={}, productoId={}", 
                    doc.getObjectId("_id"), doc.getObjectId("productoId"));
            }
            
            // Buscar si existe el item directamente como documento individual (no como parte de un array)
            boolean eliminado = false;
            
            // Verificar si itemId es el ID del documento completo (estructura plana)
            Document itemDoc = mongoCrud.getCollection().find(
                Filters.and(
                    Filters.eq("_id", itemId),
                    Filters.eq("usuarioId", usuarioId)
                )
            ).first();
            
            if (itemDoc != null) {
                logger.info("Item encontrado como documento principal: {}", itemDoc.getObjectId("_id"));
                mongoCrud.getCollection().deleteOne(Filters.eq("_id", itemId));
                eliminado = true;
                logger.info("Item eliminado como documento principal");
            } 
            
            // Si no se encontró como documento principal, buscar en formato de array
            if (!eliminado) {
                logger.info("Intentando eliminar como elemento dentro de un array 'items'");
                Document carrito = mongoCrud.getCollection().find(
                    Filters.eq("usuarioId", usuarioId.toString())
                ).first();
                
                if (carrito != null) {
                    Document updateQuery = new Document(
                        "$pull", 
                        new Document("items", new Document("_id", itemId))
                    );
                    
                    mongoCrud.updateWithFilter(
                        new Document("_id", carrito.getObjectId("_id")),
                        updateQuery
                    );
                    
                    eliminado = true;
                    logger.info("Item eliminado del array 'items'");
                }
            }
            
            if (!eliminado) {
                logger.warn("No se pudo encontrar el item {} para el usuario {}", itemId, usuarioId);
            }
            
            // Registrar el estado después de la eliminación
            List<Document> documentosPosteriores = mongoCrud.getCollection().find(
                Filters.eq("usuarioId", usuarioId)
            ).into(new ArrayList<>());
            
            logger.info("Documentos asociados al usuario después de eliminar: {}", documentosPosteriores.size());
        } catch (Exception e) {
            logger.error("Error al eliminar item del carrito: ", e);
            throw new RuntimeException("Error al eliminar item del carrito: " + e.getMessage(), e);
        }
    }
    
    public List<CarritoItem> obtenerCarrito(ObjectId usuarioId) {
        try {
            logger.info("Obteniendo carrito para usuario: {}", usuarioId);
            List<CarritoItem> items = new ArrayList<>();
            
            // Si no hay ID de usuario, devolver lista vacía
            if (usuarioId == null) {
                logger.warn("ID de usuario es null, retornando carrito vacío");
                return items;
            }

            // Buscar todos los documentos que pertenecen al usuario
            List<Document> documentos = mongoCrud.getCollection().find(
                Filters.eq("usuarioId", usuarioId)
            ).into(new ArrayList<>());
            
            logger.info("Documentos encontrados para usuario {}: {}", usuarioId, documentos.size());

            for (Document doc : documentos) {
                try {
                    CarritoItem item = new CarritoItem();
                    
                    // ID del item (del documento)
                    item.setId(doc.getObjectId("_id"));
                    
                    // ID del producto
                    Object prodIdObj = doc.get("productoId");
                    if (prodIdObj instanceof ObjectId) {
                        item.setProductoId((ObjectId) prodIdObj);
                    } else if (prodIdObj instanceof String) {
                        item.setProductoId(new ObjectId((String) prodIdObj));
                    }
                    
                    // Cantidad, precio, título
                    item.setCantidad(doc.getInteger("cantidad", 1));
                    item.setPrecioUnitario(doc.get("precioUnitario") != null ? doc.getDouble("precioUnitario") : 0.0);
                    item.setTitulo(doc.getString("titulo"));
                    
                    // URL de la imagen
                    Object imagenIdObj = doc.get("imagenId");
                    if (imagenIdObj instanceof ObjectId) {
                        String imagenUrl = "/api/imagen/" + ((ObjectId) imagenIdObj).toHexString();
                        item.setImagenUrl(imagenUrl);
                    } else if (imagenIdObj instanceof String) {
                        String imagenUrl = "/api/imagen/" + imagenIdObj;
                        item.setImagenUrl(imagenUrl);
                    } else {
                        item.setImagenUrl("/img/no-image.jpg");
                    }
                    
                    items.add(item);
                    logger.debug("Item agregado al carrito: id={}, productoId={}, título={}",
                        item.getId(), item.getProductoId(), item.getTitulo());
                } catch (Exception e) {
                    logger.error("Error al procesar documento del carrito: {}", doc.toJson(), e);
                }
            }
            
            logger.info("Carrito obtenido con {} items", items.size());
            return items;
        } catch (Exception e) {
            logger.error("Error al obtener el carrito: ", e);
            return new ArrayList<>();
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

    /**
     * Limpia completamente el carrito de un usuario
     * 
     * @param usuarioId ID del usuario
     */
    public void limpiarCarrito(ObjectId usuarioId) {
        logger.info("Limpiando carrito para usuario: {}", usuarioId);
        
        try {
            // Configurar la colección
            mongoCrud.setCollection("carritos");
            
            // Eliminar todos los items del carrito del usuario
            Document filtro = new Document("usuarioId", usuarioId);
            long documentosEliminados = mongoCrud.getCollection().deleteMany(filtro).getDeletedCount();
            
            logger.info("Documentos eliminados del carrito: {}", documentosEliminados);
            
            // Alternativa: si los documentos tienen "usuarioId" como string en lugar de ObjectId
            filtro = new Document("usuarioId", usuarioId.toString());
            documentosEliminados = mongoCrud.getCollection().deleteMany(filtro).getDeletedCount();
            
            logger.info("Documentos adicionales eliminados (usuarioId como string): {}", documentosEliminados);
        } catch (Exception e) {
            logger.error("Error al limpiar carrito: ", e);
            throw new RuntimeException("Error al limpiar carrito: " + e.getMessage(), e);
        }
    }

    public List<Publicacion> obtenerProductosRelacionados(ObjectId usuarioId) {
        try {
            // 1. Obtener categorías de los productos en el carrito
            List<String> categoriasEnCarrito = new ArrayList<>();
            List<CarritoItem> itemsCarrito = obtenerCarrito(usuarioId);
            
            for (CarritoItem item : itemsCarrito) {
                Publicacion publicacion = publicacionServicio.obtenerPublicacionPorId(item.getProductoId());
                if (publicacion != null && !categoriasEnCarrito.contains(publicacion.getCategoria())) {
                    categoriasEnCarrito.add(publicacion.getCategoria());
                }
            }

            // 2. Si el carrito está vacío, obtener productos aleatorios
            if (categoriasEnCarrito.isEmpty()) {
                return publicacionServicio.obtenerPublicacionesRecientes(3);
            }

            // 3. Obtener un producto por cada categoría
            List<Publicacion> productosRelacionados = new ArrayList<>();
            
            for (String categoria : categoriasEnCarrito) {
                List<Publicacion> publicacionesCategoria = 
                    publicacionServicio.obtenerPublicacionesRecientesPorCategoria(categoria, 1);
                if (!publicacionesCategoria.isEmpty()) {
                    productosRelacionados.add(publicacionesCategoria.get(0));
                }
                
                // Si ya tenemos 3 productos, salimos
                if (productosRelacionados.size() >= 3) {
                    break;
                }
            }

            // 4. Si no tenemos 3 productos, completar con productos aleatorios
            if (productosRelacionados.size() < 3) {
                List<Publicacion> productosAdicionales = 
                    publicacionServicio.obtenerPublicacionesRecientes(3 - productosRelacionados.size());
                productosRelacionados.addAll(productosAdicionales);
            }

            return productosRelacionados;

        } catch (Exception e) {
            logger.error("Error al obtener productos relacionados: ", e);
            return new ArrayList<>();
        }
    }
}