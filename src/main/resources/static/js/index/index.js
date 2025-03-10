function createProductCard(product) {
    const card = document.createElement('div');
    card.classList.add('product-card');

    // Imagen del producto
    const imageContainer = document.createElement('div');
    imageContainer.classList.add('product-image');
    const img = document.createElement('img');
    
    // Obtener el ID de imagen correcto
    const imagenId = product.imagenIdAsString || 
                    (product.imagenId && product.imagenId.$oid) || 
                    product.imagenId;

    if (imagenId) {
        img.src = `/api/imagen/${imagenId}`;
        console.log('URL de imagen:', img.src); // Debug
    } else {
        img.src = '/img/placeholder/no-image.svg';
        console.warn('No se encontró ID de imagen para:', product);
    }

    img.alt = product.titulo;
    img.onerror = () => {
        console.warn('Error al cargar imagen:', img.src);
        img.src = '/img/placeholder/no-image.svg';
    };

    // Agregar clases para el tamaño
    img.classList.add('w-full', 'h-full', 'object-cover');
    
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

    // Función para obtener el ID correcto del producto
    function getProductId(product) {
        console.log('Procesando producto para ID:', product); // Debug

        // Si tenemos idAsString, usarlo directamente
        if (product.idAsString) {
            return product.idAsString;
        }

        // Si es un string directo
        if (typeof product.id === 'string') {
            return product.id;
        }

        // Si es un ObjectId de MongoDB
        if (product.id && product.id.$oid) {
            return product.id.$oid;
        }

        // Si tenemos _id
        if (product._id) {
            if (typeof product._id === 'string') {
                return product._id;
            }
            if (product._id.$oid) {
                return product._id.$oid;
            }
        }

        // Si tenemos id como objeto
        if (typeof product.id === 'object') {
            return product.id.toString();
        }

        console.warn('No se pudo obtener ID del producto:', product);
        return null;
    }

    // Rating con botones interactivos
    const rating = document.createElement('div');
    rating.classList.add('product-rating');
    
    const likeBtn = document.createElement('button');
    likeBtn.classList.add('rating-btn', 'likes');
    likeBtn.innerHTML = `<i class="fas fa-thumbs-up"></i> <span>${product.ratingLikes || 0}</span>`;
    
    const dislikeBtn = document.createElement('button');
    dislikeBtn.classList.add('rating-btn', 'dislikes');
    dislikeBtn.innerHTML = `<i class="fas fa-thumbs-down"></i> <span>${product.ratingDislikes || 0}</span>`;

    // Función para manejar el rating
    async function handleRating(tipo) {
        try {
            const productoId = getProductId(product);
            
            if (!productoId) {
                throw new Error('ID de producto no válido');
            }

            console.log('Enviando rating para producto:', productoId); // Debug

            // Corregir la ruta del endpoint
            const response = await fetch(`/api/publicaciones/${productoId}/rating/${tipo}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                credentials: 'include'
            });

            console.log('Respuesta:', response.status); // Debug

            if (!response.ok) {
                if (response.status === 401) {
                    alert('Debes iniciar sesión para calificar productos');
                    window.location.href = '/auth/login';
                    return;
                }
                const errorData = await response.json();
                throw new Error(errorData.mensaje || `Error en la respuesta: ${response.status}`);
            }

            const data = await response.json();
            console.log('Respuesta del servidor:', data); // Debug
            
            // Actualizar UI
            likeBtn.querySelector('span').textContent = data.likes;
            dislikeBtn.querySelector('span').textContent = data.dislikes;
            
            // Efectos visuales
            const btn = tipo === 'like' ? likeBtn : dislikeBtn;
            btn.classList.add('active');
            setTimeout(() => btn.classList.remove('active'), 200);
            
        } catch (error) {
            console.error('Error detallado:', error);
            alert('Error al actualizar la calificación: ' + error.message);
        }
    }

    likeBtn.addEventListener('click', () => handleRating('like'));
    dislikeBtn.addEventListener('click', () => handleRating('dislike'));

    rating.appendChild(likeBtn);
    rating.appendChild(dislikeBtn);
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


async function loadFeaturedProducts() {
    const container = document.getElementById('featuredProductsContainer');
    console.log('Cargando productos destacados...');
    
    try {
        const response = await fetch('/api/publicaciones/destacados?limite=6');
        console.log('Estado de respuesta destacados:', response.status);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('Productos destacados recibidos:', data);
        
        if (!data || data.length === 0) {
            container.innerHTML = '<p class="text-center text-gray-500">No hay productos destacados disponibles</p>';
            return;
        }

        // Ordenar por número de likes
        data.sort((a, b) => (b.ratingLikes || 0) - (a.ratingLikes || 0));
        
        // Renderizar con un indicador de "Destacado"
        container.innerHTML = '';
        data.forEach(publicacion => {
            const card = createProductCard(publicacion);
            
            // Agregar badge de destacado
            const badge = document.createElement('div');
            badge.classList.add('featured-badge');
            badge.innerHTML = `
                <i class="fas fa-star"></i>
                <span>${publicacion.ratingLikes || 0} likes</span>
            `;
            card.querySelector('.product-info').prepend(badge);
            
            container.appendChild(card);
        });
    } catch (error) {
        console.error('Error al cargar destacados:', error);
        container.innerHTML = '<p class="text-center text-red-500">Error al cargar los productos destacados</p>';
    }
}

document.addEventListener("DOMContentLoaded", function () {
    loadFeaturedProducts(); // Cargar productos destacados
});