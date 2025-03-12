document.addEventListener('DOMContentLoaded', function() {
    // Recuperar datos del carrito del sessionStorage
    const carritoDataStr = sessionStorage.getItem('carritoData');
    const clienteDataStr = sessionStorage.getItem('clienteData');
    
    if (!carritoDataStr) {
        console.warn('No se encontraron datos del carrito en sessionStorage');
        mostrarError('No se encontraron datos de la compra. Por favor, realiza una compra primero.');
        return;
    }
    
    try {
        // Procesar datos del carrito
        const carritoData = JSON.parse(carritoDataStr);
        console.log('Datos de la compra cargados:', carritoData);
        
        // Generar ID de factura único
        const facturaId = carritoData.facturaId || generarFacturaId();
        document.getElementById('facturaId').textContent = `#${facturaId}`;
        
        // Establecer fecha de la compra
        const fechaCompra = carritoData.fechaPago ? 
            new Date(carritoData.fechaPago) : new Date();
        document.getElementById('fechaCompra').textContent = formatearFecha(fechaCompra);
        
        // Procesar datos del cliente si existen
        if (clienteDataStr) {
            const clienteData = JSON.parse(clienteDataStr);
            document.getElementById('clienteNombre').textContent = clienteData.nombre || 'Cliente';
            document.getElementById('clienteDireccion').textContent = clienteData.direccion || 'Dirección no especificada';
            document.getElementById('clienteCiudad').textContent = 
                `${clienteData.ciudad || ''}, ${clienteData.estado || ''}, ${clienteData.cp || ''}`;
            document.getElementById('clienteTelefono').textContent = 
                `Teléfono: ${clienteData.telefono || 'No especificado'}`;
        } else {
            console.warn('No se encontraron datos de cliente');
        }
        
        // Mostrar detalles de los productos
        const productosContainer = document.getElementById('productos-lista');
        let subtotal = 0;
        
        if (carritoData.items && carritoData.items.length > 0) {
            carritoData.items.forEach(item => {
                const precio = parseFloat(item.precioUnitario) || 0;
                const cantidad = parseInt(item.cantidad) || 0;
                const totalItem = precio * cantidad;
                subtotal += totalItem;
                
                const productoHTML = `
                    <tr>
                        <td>${item.titulo || 'Producto sin nombre'}</td>
                        <td>${cantidad}</td>
                        <td>US$ ${precio.toFixed(2)}</td>
                        <td class="text-right">US$ ${totalItem.toFixed(2)}</td>
                    </tr>
                `;
                
                productosContainer.innerHTML += productoHTML;
            });
        } else {
            productosContainer.innerHTML = `
                <tr>
                    <td colspan="4" class="text-center">No hay productos en la factura</td>
                </tr>
            `;
        }
        
        // Calcular totales
        const impuestoRate = 0.18; // 18% IVA
        const impuesto = subtotal * impuestoRate;
        const envio = 5.00; // Costo fijo de envío
        const total = subtotal + impuesto + envio;
        
        // Mostrar resumen
        document.getElementById('subtotal').textContent = `US$ ${subtotal.toFixed(2)}`;
        document.getElementById('impuestos').textContent = `US$ ${impuesto.toFixed(2)}`;
        document.getElementById('costoEnvio').textContent = `US$ ${envio.toFixed(2)}`;
        document.getElementById('total').textContent = `US$ ${total.toFixed(2)}`;
        
        // Guardar una copia de los datos de compra en localStorage
        // Esto permite al usuario ver su última factura incluso después de cerrar el navegador
        localStorage.setItem('ultimaFactura', JSON.stringify({
            facturaId: facturaId,
            fechaCompra: fechaCompra.toISOString(),
            items: carritoData.items,
            subtotal: subtotal,
            impuesto: impuesto,
            envio: envio,
            total: total,
            clienteData: clienteDataStr ? JSON.parse(clienteDataStr) : null
        }));
        
    } catch (error) {
        console.error('Error al procesar datos de la compra:', error);
        mostrarError('Error al cargar los detalles de la compra: ' + error.message);
    }
});

// Función para generar un ID de factura único y amigable
function generarFacturaId() {
    const prefijo = 'HM';
    const timestamp = new Date().getTime().toString().slice(-6);
    const aleatorio = Math.floor(Math.random() * 10000).toString().padStart(4, '0');
    return `${prefijo}${timestamp}-${aleatorio}`;
}

// Función para formatear fechas en español
function formatearFecha(fecha) {
    const opciones = { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };
    
    try {
        return fecha.toLocaleDateString('es-ES', opciones);
    } catch (error) {
        console.error('Error al formatear fecha:', error);
        return fecha.toString();
    }
}

// Función para mostrar mensaje de error
function mostrarError(mensaje) {
    const container = document.querySelector('.factura-container');
    if (container) {
        container.innerHTML = `
            <div class="error-container" style="text-align: center; padding: 40px 20px;">
                <i class="fas fa-exclamation-circle" style="color: #dc3545; font-size: 48px; margin-bottom: 20px;"></i>
                <h2 style="color: #dc3545; margin-bottom: 15px;">Error</h2>
                <p style="color: #555; margin-bottom: 25px;">${mensaje}</p>
                <a href="/" class="btn" style="text-decoration: none;">Volver al inicio</a>
            </div>
        `;
    }
}

// Función para descargar o imprimir la factura
function imprimirFactura() {
    window.print();
}

// Exponer función para poder llamarla desde HTML
window.imprimirFactura = imprimirFactura;