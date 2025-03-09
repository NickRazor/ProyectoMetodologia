class CarritoService {
    static async agregarAlCarrito(productoId, cantidad) {
        try {
            // Validación del ID
            if (!productoId || typeof productoId !== 'string') {
                throw new Error('ID de producto debe ser un string válido');
            }

            const productoIdStr = productoId.trim();
            if (productoIdStr === '') {
                throw new Error('ID de producto inválido');
            }

            console.log('Enviando al servidor:', {
                productoId: productoIdStr,
                cantidad: cantidad
            });

            const response = await fetch('/api/carrito/agregar', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({
                    productoId: productoIdStr,
                    cantidad: parseInt(cantidad)
                })
            });

            // Primero obtenemos el texto de la respuesta
            const responseText = await response.text();
            console.log('Respuesta del servidor (texto):', responseText);

            if (!response.ok) {
                throw new Error(responseText || 'Error al agregar al carrito');
            }

            // Si hay texto, intentamos parsearlo como JSON
            let resultado;
            if (responseText.trim()) {
                try {
                    resultado = JSON.parse(responseText);
                } catch (parseError) {
                    console.log('La respuesta no es JSON. Usando texto como mensaje:', responseText);
                    resultado = { mensaje: responseText };
                }
            } else {
                resultado = { mensaje: 'Producto agregado con éxito' };
            }

            // Actualizar contador del carrito
            await this.actualizarContadorCarrito();
            
            return resultado;

        } catch (error) {
            console.error('Error en agregarAlCarrito:', error);
            throw error;
        }
    }

    static getCleanId(itemId) {
        if (typeof itemId === 'object') {
            return itemId.$oid || itemId.toString();
        }
        return itemId.toString();
    }

    static async actualizarCantidad(itemId, nuevaCantidad) {
        try {
            // Validaciones iniciales
            if (!itemId) {
                throw new Error('ID de item no proporcionado');
            }

            if (nuevaCantidad < 1) {
                throw new Error('La cantidad debe ser al menos 1');
            }

            // Procesar el ID del item de manera más robusta
            let cleanItemId;
            if (typeof itemId === 'object') {
                if (itemId.$oid) {
                    cleanItemId = itemId.$oid;
                } else if (itemId.toString) {
                    cleanItemId = itemId.toString();
                }
            } else {
                cleanItemId = itemId.toString();
            }

            // Asegurarse de que el ID sea válido
            cleanItemId = cleanItemId.trim();
            if (!cleanItemId.match(/^[0-9a-fA-F]{24}$/)) {
                throw new Error('ID de item inválido');
            }

            console.log('Actualizando cantidad:', {
                itemId: itemId,
                cleanItemId: cleanItemId,
                nuevaCantidad: nuevaCantidad
            });

            const response = await fetch(`/api/carrito/actualizar/${cleanItemId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({
                    cantidad: parseInt(nuevaCantidad)
                })
            });

            // Manejar respuesta
            const contentType = response.headers.get('content-type');
            let resultado;

            if (contentType && contentType.includes('application/json')) {
                resultado = await response.json();
            } else {
                const text = await response.text();
                if (!response.ok) {
                    throw new Error(text || 'Error al actualizar cantidad');
                }
                resultado = { mensaje: text };
            }

            // Actualizar UI si la operación fue exitosa
            if (response.ok) {
                const cantidadElement = document.querySelector(`[data-item-id="${cleanItemId}"] .quantity-value`);
                if (cantidadElement) {
                    cantidadElement.textContent = nuevaCantidad;
                }
                await this.renderizarCarrito();
            }

            return resultado;
        } catch (error) {
            console.error('Error al actualizar cantidad:', error);
            throw error;
        }
    }

    static async eliminarItem(itemId) {
        try {
            if (!itemId) {
                throw new Error('ID de item no válido');
            }

            const confirmar = confirm('¿Estás seguro de que deseas eliminar este producto del carrito?');
            if (!confirmar) return;

            console.log('Intentando eliminar item:', itemId);

            // Cambiar la URL para que coincida con el endpoint del controlador
            const response = await fetch(`/api/carrito/${itemId}`, {
                method: 'DELETE',
                headers: {
                    'Accept': 'application/json'
                },
                credentials: 'include'
            });

            const responseText = await response.text();
            console.log('Respuesta del servidor:', responseText);

            if (!response.ok) {
                throw new Error(responseText || 'Error al eliminar item');
            }

            // Eliminar el elemento del DOM
            const itemElement = document.querySelector(`[data-item-id="${itemId}"]`);
            if (itemElement) {
                itemElement.remove();
            }

            // Actualizar UI
            await this.renderizarCarrito();
            await this.actualizarContadorCarrito();

            return { mensaje: 'Item eliminado correctamente' };
        } catch (error) {
            console.error('Error al eliminar item:', error);
            alert(`No se pudo eliminar el producto: ${error.message}`);
            throw error;
        }
    }

    static async obtenerCarrito() {
        try {
            const response = await fetch('/api/carrito', {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                },
                credentials: 'include' // Importante para enviar cookies de sesión
            });

            if (response.status === 401) {
                window.location.href = '/auth/login';
                throw new Error('Usuario no autenticado');
            }

            if (!response.ok) {
                const errorData = await response.text();
                throw new Error(errorData);
            }

            return await response.json();
        } catch (error) {
            console.error('Error en obtenerCarrito:', error);
            return { items: [], total: 0 }; // Valor por defecto si hay error
        }
    }

    static async actualizarContadorCarrito() {
        try {
            const response = await fetch('/api/carrito', {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                },
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('Error al obtener el carrito');
            }

            const data = await response.json();
            console.log('Datos del carrito recibidos:', data); // Debug

            const contador = document.getElementById('cartCount');
            if (contador) {
                // Verificar la estructura de datos y calcular el total
                let totalItems = 0;
                
                if (Array.isArray(data.items)) {
                    // Si data tiene una propiedad items que es un array
                    totalItems = data.items.reduce((sum, item) => sum + (item.cantidad || 0), 0);
                } else if (Array.isArray(data)) {
                    // Si data es directamente un array
                    totalItems = data.reduce((sum, item) => sum + (item.cantidad || 0), 0);
                } else {
                    console.warn('Formato de datos del carrito inesperado:', data);
                }

                // Actualizar el contador
                contador.textContent = totalItems.toString();
                contador.style.display = totalItems > 0 ? 'block' : 'none';
            } else {
                console.warn('Elemento contador del carrito no encontrado');
            }
        } catch (error) {
            console.error('Error al actualizar contador:', error);
            // No lanzar el error para evitar interrumpir la UI
        }
    }

    static async renderizarCarrito() {
        try {
            const carrito = await this.obtenerCarrito();
            const contenedorCarrito = document.querySelector('.flex-1.animate-slide-up');
            let subtotalGeneral = 0;

            console.log('Carrito recibido:', carrito);

            if (!carrito || !carrito.items || carrito.items.length === 0) {
                contenedorCarrito.innerHTML = `
                    <div class="text-center py-8">
                        <i class="fas fa-shopping-cart text-gray-400 text-5xl mb-4"></i>
                        <p class="text-gray-500">Tu carrito está vacío</p>
                        <a href="/" class="btn-blue inline-block mt-4">Ir a comprar</a>
                    </div>`;
                actualizarSubtotales(0, 0);
                return;
            }

            const cartHTML = carrito.items.map(item => {
                // Validar que el item tenga todas las propiedades necesarias
                if (!item) {
                    console.error('Item inválido:', item);
                    return '';
                }

                // Adaptar la estructura del item a nuestro formato
                const precio = parseFloat(item.precioUnitario) || 0;
                const cantidad = parseInt(item.cantidad) || 0;
                const subtotal = precio * cantidad;
                subtotalGeneral += subtotal;
                
                // Obtener el ID correcto del item
                let itemId;
                // Como objeto MongoDB
                if (typeof item.id === 'object' && item.id.$oid) {
                    itemId = item.id.$oid;
                }
                // Como string
                else if (typeof item.id === 'string') {
                    itemId = item.id;
                } else {
                    // Si el ID está en el campo productoId
                    itemId = item.productoId?.toString() || '';
                    if (!itemId) {
                        console.error('No se pudo obtener un ID válido del item:', item);
                        return '';
                    }
                }

                // Log para debugging
                console.log('ID procesado:', {
                    original: item.id,
                    procesado: itemId,
                    longitud: itemId.length
                });
                
                return `
                    <div class="flex flex-col md:flex-row gap-8 mb-6 border-b pb-6" data-item-id="${itemId}">
                        <div class="md:w-1/4">
                            <div class="rounded-lg overflow-hidden">
                                <img src="${item.imagenUrl || '/img/placeholder.png'}" 
                                     alt="${item.titulo || 'Producto'}"
                                     class="w-full h-auto object-cover"
                                     onerror="this.onerror=null; this.src='/img/placeholder.png'"/>
                            </div>
                        </div>
                        <div class="md:w-3/4">
                            <h2 class="text-xl font-semibold mb-2">${item.titulo || 'Producto sin título'}</h2>
                            <p class="text-green-600 mb-4">Disponible</p>

                            <div class="flex items-center space-x-2 mb-6">
                                <button class="quantity-btn decrease-btn" 
                                        onclick="CarritoService.actualizarCantidad('${itemId}', ${cantidad - 1})"
                                        ${cantidad <= 1 ? 'disabled style="opacity: 0.5;"' : ''}>
                                    <i class="fas fa-minus"></i>
                                </button>
                                <span class="w-8 text-center quantity-value">${cantidad}</span>
                                <button class="quantity-btn increase-btn" 
                                        onclick="CarritoService.actualizarCantidad('${itemId}', ${cantidad + 1})">
                                    <i class="fas fa-plus"></i>
                                </button>
                                <button class="ml-4 text-red-500 hover:text-red-700 transition-colors duration-300" 
                                        onclick="CarritoService.eliminarItem('${itemId}')">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>

                            <div class="flex justify-between items-center">
                                <span class="text-sm font-semibold">PRECIO UNITARIO</span>
                                <span class="text-xl font-bold">US $ ${precio.toFixed(2)}</span>
                            </div>
                            <div class="flex justify-end">
                                <span class="text-sm font-semibold subtotal">
                                    Subtotal (${cantidad} producto${cantidad > 1 ? 's' : ''}): 
                                    US $ ${subtotal.toFixed(2)}
                                </span>
                            </div>
                        </div>
                    </div>`;
            }).filter(Boolean).join('');

            contenedorCarrito.innerHTML = cartHTML;
            actualizarSubtotales(carrito.items.length, subtotalGeneral);

        } catch (error) {
            console.error('Error al renderizar carrito:', error);
            if (contenedorCarrito) {
                contenedorCarrito.innerHTML = `
                    <div class="text-center py-8">
                        <i class="fas fa-exclamation-triangle text-red-500 text-5xl mb-4"></i>
                        <p class="text-red-500">Error al cargar el carrito</p>
                        <button onclick="CarritoService.renderizarCarrito()" 
                                class="btn-blue inline-block mt-4">
                            Intentar de nuevo
                        </button>
                    </div>`;
            }
        }
    }
}

// Inicializar contador al cargar la página
document.addEventListener('DOMContentLoaded', () => {
    CarritoService.actualizarContadorCarrito()
        .catch(error => console.error('Error al inicializar contador:', error));
});

// Hacer la clase disponible globalmente
window.CarritoService = CarritoService;

// Función para actualizar los subtotales
function actualizarSubtotales(cantidadItems, subtotal) {
    // Actualizar subtotal en la sección principal
    const subtotalElement = document.querySelector('.checkout-subtotal');
    if (subtotalElement) {
        subtotalElement.textContent = `Subtotal (${cantidadItems} producto${cantidadItems > 1 ? 's' : ''}): US $ ${subtotal.toFixed(2)}`;
    }

    // Actualizar el botón de pago
    const btnPago = document.querySelector('.btn-blue.w-full');
    if (btnPago) {
        btnPago.disabled = subtotal === 0;
        btnPago.style.opacity = subtotal === 0 ? '0.5' : '1';
    }
}

// Inicializar carrito al cargar la página
document.addEventListener('DOMContentLoaded', () => {
    if (window.location.pathname.includes('/carrito')) {
        CarritoService.renderizarCarrito();
    }
});