document.addEventListener('DOMContentLoaded', function() {
    // Referencias a elementos DOM
    const searchInput = document.getElementById('searchInput');
    const searchButton = document.getElementById('searchButton');
    const searchModal = document.getElementById('searchModal');
    const closeSearchModalBtn = document.getElementById('closeSearchModal');
    const searchResults = document.getElementById('searchResults');
    const noResults = document.getElementById('noResults');
    const searchLoading = document.getElementById('searchLoading'); // Añadir esta referencia
    
    // Variable para timeout de búsqueda
    let searchTimeout;
    
    // Abrir modal al hacer clic en el botón de búsqueda
    if (searchButton) {
        searchButton.addEventListener('click', function() {
            const query = searchInput.value.trim();
            if (query.length > 0) {
                openSearchModal();
                performSearch(query);
            }
        });
    }
    
    // Abrir modal al presionar Enter en el campo de búsqueda
    if (searchInput) {
        searchInput.addEventListener('keyup', function(event) {
            if (event.key === 'Enter') {
                const query = searchInput.value.trim();
                if (query.length > 0) {
                    openSearchModal();
                    performSearch(query);
                }
            } else if (searchModal && searchModal.style.display === 'block') {
                // Si el modal ya está abierto, realizar búsqueda en tiempo real
                const query = searchInput.value.trim();
                
                // Limpiar timeout anterior para evitar múltiples búsquedas
                clearTimeout(searchTimeout);
                
                // Establecer un pequeño retraso para no hacer búsquedas con cada tecla
                searchTimeout = setTimeout(function() {
                    if (query.length > 0) {
                        performSearch(query);
                    } else {
                        if (searchResults) searchResults.innerHTML = '';
                        if (searchLoading) searchLoading.style.display = 'none';
                        if (noResults) noResults.style.display = 'block';
                    }
                }, 300);
            }
        });
    }
    
    // Cerrar modal al hacer clic en el botón de cierre
    if (closeSearchModalBtn) {
        closeSearchModalBtn.addEventListener('click', function() {
            closeSearchModal();
        });
    } else {
        console.warn('El botón para cerrar el modal no fue encontrado');
    }
    
    // Cerrar modal al hacer clic fuera del contenido
    window.addEventListener('click', function(event) {
        if (searchModal && event.target === searchModal) {
            closeSearchModal();
        }
    });
    
    // Función para abrir el modal
    function openSearchModal() {
        if (searchModal) {
            console.log('Abriendo modal de búsqueda');
            searchModal.style.display = 'block';
            document.body.style.overflow = 'hidden'; // Evitar scroll en el fondo
        } else {
            console.warn('El modal de búsqueda no fue encontrado');
        }
    }
    
    // Función para cerrar el modal
    function closeSearchModal() {
        if (searchModal) {
            console.log('Cerrando modal de búsqueda');
            searchModal.style.display = 'none';
            document.body.style.overflow = ''; // Restaurar scroll
        } else {
            console.warn('El modal de búsqueda no fue encontrado al intentar cerrarlo');
        }
    }
    
    // Función para realizar la búsqueda
    function performSearch(query) {
        // Verificar existencia de elementos antes de manipularlos
        if (searchResults) searchResults.innerHTML = '';
        if (searchLoading) searchLoading.style.display = 'block';
        if (noResults) noResults.style.display = 'none';
        
        // Hacer la solicitud AJAX para obtener productos
        fetch(`/api/publicaciones/search?query=${encodeURIComponent(query)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error en la búsqueda');
                }
                return response.json();
            })
            .then(products => {
                // Ocultar indicador de carga
                if (searchLoading) searchLoading.style.display = 'none';
                
                if (products && products.length > 0) {
                    displaySearchResults(products);
                    if (noResults) noResults.style.display = 'none';
                } else {
                    if (searchResults) searchResults.innerHTML = '';
                    if (noResults) noResults.style.display = 'block';
                }
            })
            .catch(error => {
                console.error('Error en la búsqueda:', error);
                if (searchLoading) searchLoading.style.display = 'none';
                if (noResults) noResults.style.display = 'block';
                if (searchResults) searchResults.innerHTML = `<p class="error-message">Error al buscar productos. Por favor, inténtalo de nuevo más tarde.</p>`;
            });
    }
    
    // Función mejorada para mostrar resultados
    function displaySearchResults(products) {
        if (!searchResults) return;
        
        searchResults.innerHTML = '';
        
        // Imagen de fallback para productos sin imagen
        const fallbackImage = generatePlaceholderImage();
        
        // Verificar que products sea un array
        if (!Array.isArray(products)) {
            console.error('Error: productos no es un array', products);
            if (noResults) noResults.style.display = 'block';
            return;
        }
        
        // Filtrar productos válidos
        const validProducts = products.filter(product => {
            // Un producto necesita al menos título y precio para ser mostrado
            return product && (product.titulo || product.name) && 
                   (typeof product.precio === 'number' || typeof product.price === 'number');
        });
        
        console.log(`Productos recibidos: ${products.length}, válidos: ${validProducts.length}`);
        
        if (validProducts.length === 0) {
            if (noResults) noResults.style.display = 'block';
            return;
        }
        
        validProducts.forEach((product, index) => {
            // IMPORTANTE: Inspección resumida del producto
            console.log(`Producto #${index + 1}:`, {
                titulo: product.titulo || product.name,
                precio: product.precio || product.price,
                id: product.id || product._id || product.publicacionId || null
            });
            
            // Extraer ID - PRIORIZAR EL ID MONGODB
            let productId = null;
            
            // Estrategia 1: Buscar un ID que cumpla el formato MongoDB (24 caracteres hex)
            for (const key of ['id', '_id', 'mongoId']) {
                const idValue = product[key];
                if (typeof idValue === 'string' && /^[0-9a-fA-F]{24}$/.test(idValue)) {
                    productId = idValue;
                    break;
                }
                
                // Si es objeto con $oid
                if (idValue && idValue.$oid) {
                    productId = idValue.$oid;
                    break;
                }
            }
            
            // Estrategia 2: Usar el publicacionId si no encontramos un ID MongoDB
            if (!productId && product.publicacionId) {
                productId = product.publicacionId;
            }
            
            // Estrategia 3: Usar un ID temporal si todo falla
            if (!productId) {
                productId = 'temp-' + Date.now() + '-' + index;
                console.warn('Usando ID temporal para producto:', productId);
            }
            
            // Verificar el formato del ID que estamos usando
            const isMongoFormat = /^[0-9a-fA-F]{24}$/.test(productId);
            console.log(`Producto #${index + 1} - ID: ${productId} (${isMongoFormat ? 'MongoDB' : 'Otro formato'})`);
            
            const productCard = document.createElement('div');
            productCard.className = 'search-product-card';
            
            // Determinar URL de imagen
            let imageUrl = null;
            
            if (product.imagenId) {
                const imageId = extractId(product.imagenId);
                if (imageId && imageId !== "[object Object]") {
                    imageUrl = `/api/imagen/${imageId}`;
                }
            }
            
            if (!imageUrl) {
                imageUrl = fallbackImage;
            }
            
            // Asegurar que tenemos valores válidos para mostrar
            const titulo = product.titulo || product.name || 'Producto sin nombre';
            const precio = parseFloat(product.precio || product.price || 0).toFixed(2);
            const descripcion = product.descripcion || product.description || 'Sin descripción';
            const categoria = product.categoria || product.category || 'General';
            
            // Sanear título para atributo data (evitar problemas con comillas)
            const tituloAttr = titulo.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
            
            // Construir tarjeta de producto - guarda también si es un ID MongoDB o no
            productCard.innerHTML = `
                <img class="search-product-image" src="${imageUrl}" alt="${tituloAttr}" 
                     onerror="this.onerror=null; this.src='${fallbackImage}';">
                <h4 class="search-product-title">${titulo}</h4>
                <div class="search-product-price">US$ ${precio}</div>
                <div class="search-product-description">${descripcion}</div>
                <div class="search-product-category">Categoría: ${categoria}</div>
                <button class="add-to-cart-btn" 
                        data-id="${productId}" 
                        data-title="${tituloAttr}" 
                        data-price="${precio}"
                        data-is-mongo="${isMongoFormat}">
                    <i class="fas fa-cart-plus"></i> Agregar al carrito
                </button>
            `;
            
            searchResults.appendChild(productCard);
            
            // Agregar event listener para el botón de carrito
            const addButton = productCard.querySelector('.add-to-cart-btn');
            addButton.addEventListener('click', function() {
                const id = this.getAttribute('data-id');
                const title = this.getAttribute('data-title');
                const price = parseFloat(this.getAttribute('data-price'));
                
                // Pasar objeto con datos completos
                addToCartFromSearch(id, {
                    titulo: title,
                    precio: price
                });
            });
        });
    }

    // Verificación de elementos del modal en carga
    console.log('Estado de los elementos del modal:', {
        searchModal: !!searchModal,
        closeBtn: !!closeSearchModalBtn,
        searchResults: !!searchResults,
        searchLoading: !!searchLoading,
        noResults: !!noResults
    });
    
    // Ejecutar diagnóstico después de cargar la página
    setTimeout(diagnoseAPI, 3000);
});

// Función mejorada para agregar al carrito
function addToCartFromSearch(productId, productData = null) {
    try {
        console.log('Intentando agregar al carrito:', { id: productId, data: productData });
        
        if (!productId || productId === "undefined" || productId === "[object Object]") {
            showNotification('Error: ID de producto no válido', 'error');
            return;
        }
        
        // NUEVA VALIDACIÓN: Verificar si es un ID MongoDB válido (24 caracteres hexadecimales)
        const isValidMongoId = /^[0-9a-fA-F]{24}$/.test(productId);
        
        // Si no es un ID MongoDB válido pero empieza con "PUB-", es un ID amigable
        if (!isValidMongoId && productId.startsWith('PUB-')) {
            // En este caso, necesitamos obtener el ObjectId real del backend
            console.log('Se detectó un publicacionId amigable. Buscando ID MongoDB correspondiente...');
            
            // Buscar el producto por su publicacionId amigable
            fetch(`/api/publicaciones/por-publicacion-id/${encodeURIComponent(productId)}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Error al buscar producto: ${response.status}`);
                    }
                    return response.json();
                })
                .then(producto => {
                    // Extraer el ID MongoDB real
                    const mongoId = extractMongoId(producto);
                    if (!mongoId) {
                        throw new Error('No se pudo obtener un ID MongoDB válido');
                    }
                    
                    console.log('Se obtuvo ID MongoDB:', mongoId);
                    // Llamar recursivamente con el ID correcto
                    addToCartFromSearch(mongoId, {
                        titulo: producto.titulo || productData?.titulo || 'Producto',
                        precio: producto.precio || productData?.precio || 0
                    });
                })
                .catch(error => {
                    console.error('Error al obtener ID MongoDB:', error);
                    showNotification('No se pudo agregar el producto al carrito', 'error');
                });
            
            return;
        }
        
        // Verificar CarritoService
        if (window.CarritoService && typeof CarritoService.agregarAlCarrito === 'function') {
            // Si ya tenemos los datos del producto (desde el botón), usarlos directamente
            if (productData && productData.titulo) {
                // Versión simplificada que evita una solicitud extra
                return CarritoService.agregarAlCarrito(productId, 1)
                    .then(() => {
                        showNotification(`${productData.titulo} agregado al carrito`, 'success');
                        if (typeof CarritoService.actualizarContadorCarrito === 'function') {
                            CarritoService.actualizarContadorCarrito();
                        }
                    })
                    .catch(err => {
                        console.error('Error al agregar al carrito:', err);
                        showNotification('No se pudo agregar el producto al carrito', 'error');
                    });
            }
            
            // Si no tenemos datos, intentar obtenerlos del API
            fetch(`/api/publicaciones/${productId}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Error al obtener producto: ${response.status}`);
                    }
                    return response.json();
                })
                .then(producto => {
                    return CarritoService.agregarAlCarrito(productId, 1)
                        .then(() => {
                            showNotification(`${producto.titulo || 'Producto'} agregado al carrito`, 'success');
                            if (typeof CarritoService.actualizarContadorCarrito === 'function') {
                                CarritoService.actualizarContadorCarrito();
                            }
                        });
                })
                .catch(error => {
                    console.error('Error:', error);
                    // Si falla la obtención, intentar agregar directamente
                    return CarritoService.agregarAlCarrito(productId, 1)
                        .then(() => {
                            showNotification('Producto agregado al carrito', 'success');
                            if (typeof CarritoService.actualizarContadorCarrito === 'function') {
                                CarritoService.actualizarContadorCarrito();
                            }
                        })
                        .catch(err => {
                            console.error('Error al agregar al carrito:', err);
                            showNotification('No se pudo agregar el producto al carrito', 'error');
                        });
                });
        } else {
            console.warn('CarritoService no está disponible');
            showNotification('El servicio de carrito no está disponible', 'error');
        }
    } catch (error) {
        console.error('Error general:', error);
        showNotification('Error al procesar la solicitud', 'error');
    }
}

// Función auxiliar para extraer el ID MongoDB de un producto
function extractMongoId(product) {
    // Si el product.id es un string que coincide con el patrón de MongoDB
    if (typeof product.id === 'string' && /^[0-9a-fA-F]{24}$/.test(product.id)) {
        return product.id;
    }
    
    // Si product.id es un objeto con $oid
    if (product.id && product.id.$oid) {
        return product.id.$oid;
    }
    
    // Si product._id existe
    if (product._id) {
        if (typeof product._id === 'string' && /^[0-9a-fA-F]{24}$/.test(product._id)) {
            return product._id;
        }
        if (product._id.$oid) {
            return product._id.$oid;
        }
    }
    
    // Si ningún método anterior funciona, buscamos cualquier propiedad que parezca un ID MongoDB
    for (const key in product) {
        const value = product[key];
        if (typeof value === 'string' && /^[0-9a-fA-F]{24}$/.test(value)) {
            return value;
        }
        if (value && value.$oid) {
            return value.$oid;
        }
    }
    
    return null;
}

// Agregar una función para mostrar notificaciones
function showNotification(message, type = 'success') {
    // Crear elemento de notificación
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    
    // Contenido según tipo
    if (type === 'success') {
        notification.innerHTML = `
            <div class="notification-content">
                <i class="fas fa-check-circle"></i>
                <span>${message}</span>
            </div>
        `;
    } else {
        notification.innerHTML = `
            <div class="notification-content">
                <i class="fas fa-exclamation-circle"></i>
                <span>${message}</span>
            </div>
        `;
    }
    
    // Agregar al DOM
    document.body.appendChild(notification);
    
    // Mostrar con animación
    setTimeout(() => notification.classList.add('show'), 10);
    
    // Eliminar después de 3 segundos
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Estilos para notificaciones (se añaden mediante JavaScript)
document.addEventListener('DOMContentLoaded', function() {
    if (!document.getElementById('notification-styles')) {
        const style = document.createElement('style');
        style.id = 'notification-styles';
        style.textContent = `
            .notification {
                position: fixed;
                bottom: 20px;
                right: 20px;
                padding: 12px 15px;
                border-radius: 4px;
                box-shadow: 0 3px 10px rgba(0, 0, 0, 0.2);
                transform: translateX(120%);
                transition: transform 0.3s ease;
                z-index: 9999;
                max-width: 300px;
            }
            
            .notification.show {
                transform: translateX(0);
            }
            
            .notification.success {
                background-color: #4CAF50;
                color: white;
            }
            
            .notification.error {
                background-color: #f44336;
                color: white;
            }
            
            .notification-content {
                display: flex;
                align-items: center;
            }
            
            .notification-content i {
                margin-right: 10px;
                font-size: 18px;
            }
        `;
        document.head.appendChild(style);
    }
});

// Versión mejorada de extractId que maneja objetos de fecha y otros casos especiales
function extractId(idObject) {
    // Si es null o undefined
    if (idObject == null) return null;
    
    // Si ya es un string, devolverlo directamente
    if (typeof idObject === 'string') {
        return idObject;
    }
    
    // Si es un objeto con $oid (formato JSON de MongoDB)
    if (idObject.$oid) {
        return idObject.$oid;
    }
    
    // Si es un objeto, intentar extraer un ID de él
    if (typeof idObject === 'object') {
        // Verificar si es un objeto de fecha (solo tiene timestamp y date)
        if (idObject.timestamp && idObject.date && Object.keys(idObject).length <= 2) {
            console.log('Detectado objeto de fecha, no contiene un ID válido');
            return null;
        }
        
        // Propiedades comunes de ID
        const idProps = ['id', '_id', 'ID', 'publicacionId', 'idPublicacion', 'publicacion_id'];
        
        // Buscar cualquier propiedad de ID válida
        for (const prop of idProps) {
            if (idObject[prop]) {
                const propValue = idObject[prop];
                if (typeof propValue === 'string') {
                    return propValue;
                } else if (typeof propValue === 'object') {
                    // Llamada recursiva para objetos anidados
                    const nestedId = extractId(propValue);
                    if (nestedId) return nestedId;
                }
            }
        }
        
        // Si tiene una propiedad que incluye "id" en su nombre
        for (const key in idObject) {
            if (key.toLowerCase().includes('id') && typeof idObject[key] === 'string') {
                return idObject[key];
            }
        }
        
        // Intentar obtener una representación de cadena
        try {
            const strValue = idObject.toString();
            // Asegurarse de que no sea la representación por defecto
            if (strValue && strValue !== '[object Object]') {
                return strValue;
            }
        } catch (e) {
            console.warn('Error al convertir ID a string:', e);
        }
    }
    
    // Log si no se pudo extraer ID
    console.warn('No se pudo extraer ID de:', idObject);
    return null;
}

// Función para generar una imagen de marcador de posición dinámica en base64
function generatePlaceholderImage() {
    // Crear un canvas para generar la imagen
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    
    // Configurar dimensiones
    canvas.width = 200;
    canvas.height = 200;
    
    // Fondo
    ctx.fillStyle = '#f5f5f5';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    // Dibujar un icono de producto
    ctx.fillStyle = '#dddddd';
    ctx.beginPath();
    ctx.moveTo(70, 70);
    ctx.lineTo(130, 70);
    ctx.lineTo(150, 100);
    ctx.lineTo(130, 130);
    ctx.lineTo(70, 130);
    ctx.lineTo(50, 100);
    ctx.closePath();
    ctx.fill();
    
    // Borde
    ctx.strokeStyle = '#cccccc';
    ctx.lineWidth = 2;
    ctx.stroke();
    
    // Texto
    ctx.font = '14px Arial';
    ctx.fillStyle = '#888888';
    ctx.textAlign = 'center';
    ctx.fillText('Imagen no disponible', canvas.width/2, 160);
    
    // Convertir a data URL (formato base64)
    return canvas.toDataURL('image/png');
}

// Verificar que el controlador de API existe y funciona correctamente
document.addEventListener('DOMContentLoaded', function() {
    // Verificar rutas API
    fetch('/api/health')
        .then(response => response.json())
        .catch(error => {
            console.warn('API de verificación no disponible:', error);
        });
});

// Función para inspeccionar objetos problemáticos
function inspectObject(obj, title = 'Inspección de objeto') {
    console.group(title);
    
    try {
        // Propiedades básicas
        console.log('Tipo:', typeof obj);
        console.log('Constructor:', obj?.constructor?.name);
        
        if (obj === null) {
            console.log('Valor: null');
        } else if (obj === undefined) {
            console.log('Valor: undefined');
        } else if (typeof obj === 'object') {
            // Listar propiedades
            console.log('Propiedades:', Object.getOwnPropertyNames(obj));
            
            // Intentar mostrar valores
            const props = {};
            for (const key in obj) {
                try {
                    const value = obj[key];
                    props[key] = {
                        type: typeof value,
                        value: (typeof value === 'object') ? '[objeto]' : value
                    };
                } catch (e) {
                    props[key] = { error: e.message };
                }
            }
            console.table(props);
            
            // Intentar serializar
            try {
                console.log('JSON:', JSON.stringify(obj));
            } catch (e) {
                console.log('Error al serializar:', e.message);
            }
        } else {
            console.log('Valor:', obj);
        }
    } catch (e) {
        console.error('Error durante la inspección:', e);
    }
    
    console.groupEnd();
}

// Agregar un helper para detectar problemas en la carga de la página
document.addEventListener('DOMContentLoaded', function() {
    console.log('Versión del script search.js: 2023-03-12.2');
    
    // Verificar si estamos en una página con resultados de búsqueda
    setTimeout(() => {
        const searchResults = document.getElementById('searchResults');
        if (searchResults) {
            console.log('Elementos de resultados de búsqueda:', {
                childNodes: searchResults.childNodes.length,
                innerHTML: searchResults.innerHTML.length > 0
            });
        }
    }, 2000);
});

// Función para diagnosticar problemas con la API
function diagnoseAPI() {
    console.group('🔍 Diagnóstico de API');
    
    // 1. Verificar la ruta /api/publicaciones/search
    fetch('/api/publicaciones/search?query=test')
        .then(response => {
            console.log('API de búsqueda:', {
                status: response.status,
                ok: response.ok,
                contentType: response.headers.get('content-type')
            });
            return response.text();
        })
        .then(text => {
            try {
                // Intentar parsear como JSON
                const data = JSON.parse(text);
                console.log('Búsqueda devuelve JSON válido:', {
                    tipo: Array.isArray(data) ? 'array' : typeof data,
                    elementos: Array.isArray(data) ? data.length : 'N/A',
                    muestra: Array.isArray(data) && data.length > 0 ? data[0] : data
                });
            } catch (e) {
                console.error('Búsqueda no devuelve JSON válido:', {
                    error: e.message,
                    primeros100Caracteres: text.substring(0, 100)
                });
            }
        })
        .catch(error => {
            console.error('Error en verificación de API:', error);
        });
    
    console.groupEnd();
}