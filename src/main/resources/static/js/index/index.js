function createProductCard(product) {
    const card = document.createElement('div');
    card.classList.add('product-card');

    // Imagen del producto
    const imageContainer = document.createElement('div');
    imageContainer.classList.add('product-image');
    const img = document.createElement('img');
    img.src = `/api/imagen/${product.imagenIdAsString}`;
    img.alt = product.titulo;
    img.onerror = () => {
        img.src = '/img/placeholder.jpg';
    };
    imageContainer.appendChild(img);
    card.appendChild(imageContainer);

    // Contenedor de información
    const infoContainer = document.createElement('div');
    infoContainer.classList.add('product-info');

    // Título
    const title = document.createElement('h3');
    title.textContent = product.titulo;
    infoContainer.appendChild(title);

    // Categoría
    const category = document.createElement('span');
    category.classList.add('product-category');
    // Mapeo de categorías actualizado
    const categoriasNombres = {
        'electronics': 'Electrónicos',
        'home': 'Casa y Jardín',
        'toys': 'Juguetes',
        'watches': 'Relojes',
        'clothing': 'Ropa'
    };
    console.log('Categoría recibida:', product.categoria); // Debug
    category.textContent = product.categoria ? 
        (categoriasNombres[product.categoria.toLowerCase()] || product.categoria) : 
        'Sin categoría';
    infoContainer.appendChild(category);

    // Descripción
    const description = document.createElement('p');
    description.textContent = product.descripcion;
    description.classList.add('product-description');
    infoContainer.appendChild(description);

    // Fecha de publicación
    const date = document.createElement('small');
    date.classList.add('product-date');
    const fecha = new Date(product.fecha);
    date.textContent = `Publicado: ${fecha.toLocaleDateString()}`;
    infoContainer.appendChild(date);

    // Rating
    const rating = document.createElement('div');
    rating.classList.add('product-rating');
    rating.innerHTML = `
        <span class="likes"><i class="fas fa-thumbs-up"></i> ${product.ratingLikes || 0}</span>
        <span class="dislikes"><i class="fas fa-thumbs-down"></i> ${product.ratingDislikes || 0}</span>
    `;
    infoContainer.appendChild(rating);

    card.appendChild(infoContainer);

    // Footer: precio y botón
    const footer = document.createElement('div');
    footer.classList.add('product-footer');
    
    const priceSpan = document.createElement('span');
    priceSpan.classList.add('price');
    priceSpan.textContent = `$${product.precio.toFixed(2)}`;
    footer.appendChild(priceSpan);
    
    const btn = document.createElement('button');
    btn.classList.add('btn', 'btn-secondary');
    btn.innerHTML = '<i class="fas fa-cart-plus"></i> Añadir al carrito';
    
    // Agregar evento click al botón
    btn.addEventListener('click', async (e) => {
        e.preventDefault();
        try {
            const shouldAdd = confirm('¿Deseas agregar este producto al carrito?');
            if (shouldAdd) {
                // Obtener el ID del producto correctamente
                const productoId = product.idAsString || product.id?.toString();
                
                // Log para debugging
                console.log('Datos del producto:', {
                    producto: product,
                    idAEnviar: productoId
                });
                
                if (!productoId) {
                    throw new Error('ID de producto no disponible');
                }

                // Mostrar indicador de carga
                btn.disabled = true;
                btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Agregando...';

                try {
                    console.log('Enviando ID al servicio:', productoId);
                    const resultado = await CarritoService.agregarAlCarrito(productoId, 1);
                    console.log('Respuesta exitosa:', resultado);
                    
                    // Actualizar UI del carrito usando CarritoService
                    await CarritoService.actualizarContadorCarrito();
                    
                    const goToCart = confirm('Producto agregado exitosamente. ¿Deseas ir al carrito?');
                    if (goToCart) {
                        window.location.href = '/carrito';
                    }
                } finally {
                    btn.disabled = false;
                    btn.innerHTML = '<i class="fas fa-cart-plus"></i> Añadir al carrito';
                }
            }
        } catch (error) {
            console.error('Error detallado:', error);
            alert(`No se pudo agregar el producto: ${error.message}`);
        }
    });
    
    footer.appendChild(btn);
    card.appendChild(footer);

    return card;
}

function renderProducts(data, container) {
    container.innerHTML = '';
    data.forEach(publicacion => {
        console.log('Creando tarjeta para:', publicacion); // Debug
        const card = createProductCard(publicacion);
        console.log('Tarjeta creada:', card); // Debug
        container.appendChild(card);
    });
}

function loadProducts(containerId) {
    const container = document.getElementById(containerId);
    console.log('Iniciando carga de productos...');
    
    fetch('/api/publicaciones')
        .then(response => {
            console.log('Estado de la respuesta:', response.status);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Datos recibidos:', data);
            if (!data || data.length === 0) {
                container.innerHTML = '<p>No hay productos disponibles</p>';
                return;
            }
            renderProducts(data, container);
        })
        .catch(error => {
            console.error('Error:', error);
            container.innerHTML = '<p>Error al cargar los productos</p>';
        });
}

document.addEventListener("DOMContentLoaded", function () {
    loadProducts("products-container");
});