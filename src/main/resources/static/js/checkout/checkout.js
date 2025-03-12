document.addEventListener('DOMContentLoaded', function() {
    cargarDatosCarrito();
    
    // Event listeners para los formularios
    const formularioEnvio = document.getElementById('formularioEnvio');
    if (formularioEnvio) {
        formularioEnvio.addEventListener('submit', procesarFormularioEnvio);
    }
    
    const formularioPago = document.getElementById('formularioPago');
    if (formularioPago) {
        formularioPago.addEventListener('submit', procesarFormularioPago);
    }
});

/**
 * Carga los datos del carrito desde sessionStorage
 */
function cargarDatosCarrito() {
    const carritoDataStr = sessionStorage.getItem('carritoData');
    
    if (!carritoDataStr) {
        console.warn('No se encontraron datos del carrito en sessionStorage');
        mostrarError('No hay productos en tu carrito. Por favor, agrega productos antes de continuar.');
        return;
    }
    
    try {
        const carritoData = JSON.parse(carritoDataStr);
        console.log('Datos del carrito cargados:', carritoData);
        
        // Mostrar resumen de compra
        mostrarResumenCompra(carritoData);
        
    } catch (error) {
        console.error('Error al procesar datos del carrito:', error);
        mostrarError('No se pudieron cargar los detalles de tu compra.');
    }
}

/**
 * Muestra el resumen de la compra en la UI
 */
function mostrarResumenCompra(carritoData) {
    const resumenContainer = document.querySelector('.resumen-compra');
    if (!resumenContainer) return;
    
    // Verificar si hay productos
    if (!carritoData.items || carritoData.items.length === 0) {
        resumenContainer.innerHTML = '<p class="text-center text-gray-500">No hay productos en tu carrito</p>';
        return;
    }
    
    // Calcular totales
    let subtotal = 0;
    let cantidadTotal = 0;
    
    // Generar HTML para cada producto
    const productosHTML = carritoData.items.map(item => {
        const precio = parseFloat(item.precioUnitario) || 0;
        const cantidad = parseInt(item.cantidad) || 0;
        const totalItem = precio * cantidad;
        
        subtotal += totalItem;
        cantidadTotal += cantidad;
        
        return `
            <div class="flex justify-between py-2 border-b">
                <div>
                    <p class="font-medium">${item.titulo || 'Producto sin título'}</p>
                    <p class="text-sm text-gray-500">Cantidad: ${cantidad}</p>
                </div>
                <p class="font-semibold">US$ ${totalItem.toFixed(2)}</p>
            </div>
        `;
    }).join('');
    
    // Calcular impuestos y total
    const impuestoRate = 0.18; // 18% de IVA
    const impuestos = subtotal * impuestoRate;
    const total = subtotal + impuestos;
    
    // Actualizar el HTML con el resumen
    resumenContainer.innerHTML = `
        <div class="mb-4">
            <h3 class="text-lg font-semibold mb-3">Resumen de compra</h3>
            <div class="space-y-2">
                ${productosHTML}
            </div>
        </div>
        
        <div class="space-y-2 border-t pt-3">
            <div class="flex justify-between">
                <p>Subtotal (${cantidadTotal} ${cantidadTotal > 1 ? 'productos' : 'producto'})</p>
                <p>US$ ${subtotal.toFixed(2)}</p>
            </div>
            <div class="flex justify-between">
                <p>Impuestos (18%)</p>
                <p>US$ ${impuestos.toFixed(2)}</p>
            </div>
            <div class="flex justify-between">
                <p>Envío</p>
                <p>US$ 0.00</p>
            </div>
            <div class="flex justify-between font-bold">
                <p>Total</p>
                <p>US$ ${total.toFixed(2)}</p>
            </div>
        </div>
    `;
    
    // Actualizar el valor oculto del total para el formulario
    const totalInput = document.getElementById('totalCompra');
    if (totalInput) {
        totalInput.value = total.toFixed(2);
    }
}

/**
 * Procesa el formulario de datos de envío
 */
function procesarFormularioEnvio(event) {
    event.preventDefault();
    
    // Obtener datos del formulario
    const formData = new FormData(event.target);
    const datosEnvio = {
        nombre: formData.get('nombre'),
        apellido: formData.get('apellido'),
        direccion: formData.get('direccion'),
        ciudad: formData.get('ciudad'),
        estado: formData.get('estado'),
        codigoPostal: formData.get('codigoPostal'),
        telefono: formData.get('telefono')
    };
    
    // Guardar datos en sessionStorage
    sessionStorage.setItem('datosEnvio', JSON.stringify(datosEnvio));
    
    // Avanzar al siguiente paso
    mostrarPasoPago();
}

/**
 * Procesa el formulario de datos de pago
 */
function procesarFormularioPago(event) {
    event.preventDefault();
    
    // Obtener datos del formulario
    const formData = new FormData(event.target);
    const datosPago = {
        numeroTarjeta: formData.get('numeroTarjeta'),
        nombreTitular: formData.get('nombreTitular'),
        fechaExpiracion: formData.get('fechaExpiracion'),
        cvv: formData.get('cvv')
    };
    
    // Simular procesamiento de pago
    console.log('Procesando pago...');
    
    // Mostrar mensaje de carga
    const btnPagar = document.getElementById('btnPagar');
    const textoOriginal = btnPagar.textContent;
    btnPagar.disabled = true;
    btnPagar.textContent = 'Procesando...';
    
    // Simular tiempo de procesamiento
    setTimeout(() => {
        // Guardar todos los datos necesarios para la factura
        const carritoData = JSON.parse(sessionStorage.getItem('carritoData') || '{}');
        const datosEnvio = JSON.parse(sessionStorage.getItem('datosEnvio') || '{}');
        
        const facturaData = {
            items: carritoData.items || [],
            total: document.getElementById('totalCompra').value,
            datosEnvio: datosEnvio,
            fechaCompra: new Date().toISOString(),
            numeroPedido: generarNumeroPedido()
        };
        
        // Guardar datos de la factura
        sessionStorage.setItem('facturaData', JSON.stringify(facturaData));
        
        // Limpiar datos del carrito y envío (ya no serán necesarios)
        sessionStorage.removeItem('carritoData');
        sessionStorage.removeItem('datosEnvio');
        
        // IMPORTANTE: Modificar esta línea para redirigir donde desees
        // Antes: window.location.href = '/factura';
        // Si quieres redirigir a otra página diferente a '/factura', cámbiala aquí:
        console.log('Pago completado. Redirigiendo a la página de factura...');
        window.location.href = '/factura';
        
    }, 2000);
}

/**
 * Muestra el paso de método de pago
 */
function mostrarPasoPago() {
    const pasoEnvio = document.getElementById('pasoEnvio');
    const pasoPago = document.getElementById('pasoPago');
    
    if (pasoEnvio && pasoPago) {
        pasoEnvio.style.display = 'none';
        pasoPago.style.display = 'block';
        
        // Actualizar indicadores de progreso
        document.querySelector('.paso-1').classList.remove('paso-activo');
        document.querySelector('.paso-1').classList.add('paso-completado');
        document.querySelector('.paso-2').classList.add('paso-activo');
    }
}

/**
 * Vuelve al paso de datos de envío
 */
function volverPasoEnvio() {
    const pasoEnvio = document.getElementById('pasoEnvio');
    const pasoPago = document.getElementById('pasoPago');
    
    if (pasoEnvio && pasoPago) {
        pasoEnvio.style.display = 'block';
        pasoPago.style.display = 'none';
        
        // Actualizar indicadores de progreso
        document.querySelector('.paso-1').classList.add('paso-activo');
        document.querySelector('.paso-1').classList.remove('paso-completado');
        document.querySelector('.paso-2').classList.remove('paso-activo');
    }
}

/**
 * Genera un número de pedido único
 */
function generarNumeroPedido() {
    const prefijo = 'PED';
    const timestamp = new Date().getTime().toString().slice(-8);
    const aleatorio = Math.floor(Math.random() * 1000).toString().padStart(3, '0');
    return `${prefijo}-${timestamp}-${aleatorio}`;
}

/**
 * Muestra un mensaje de error
 */
function mostrarError(mensaje) {
    const container = document.querySelector('.container') || document.body;
    
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-mensaje bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-6';
    errorDiv.innerHTML = `
        <strong class="font-bold">Error!</strong>
        <span class="block sm:inline">${mensaje}</span>
        <a href="/" class="block mt-2 text-blue-600 underline">Volver a la página principal</a>
    `;
    
    // Insertar al principio del contenedor
    if (container.firstChild) {
        container.insertBefore(errorDiv, container.firstChild);
    } else {
        container.appendChild(errorDiv);
    }
}

// Exponer funciones necesarias al ámbito global
window.volverPasoEnvio = volverPasoEnvio;