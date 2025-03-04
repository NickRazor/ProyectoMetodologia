function createProductCard(product) {
    const card = document.createElement('div');
    card.classList.add('product-card');

    // Imagen del producto
    const imageContainer = document.createElement('div');
    imageContainer.classList.add('product-image');
    const img = document.createElement('img');
    img.src = `/api/imagen/${product.imagenIdAsString}`; // Usar el método auxiliar
    img.alt = product.titulo;
    img.onerror = () => {
        img.src = '/img/placeholder.jpg'; // Imagen por defecto si falla la carga
    };
    imageContainer.appendChild(img);
    card.appendChild(imageContainer);

    // Título
    const title = document.createElement('h3');
    title.textContent = product.titulo;
    card.appendChild(title);

    // Descripción
    const description = document.createElement('p');
    description.textContent = product.descripcion;
    description.classList.add('product-description');
    card.appendChild(description);

    // Footer: precio y botón
    const footer = document.createElement('div');
    footer.classList.add('product-footer');
    const priceSpan = document.createElement('span');
    priceSpan.classList.add('price');
    priceSpan.textContent = `$${product.precio.toFixed(2)}`; // Formatear precio
    footer.appendChild(priceSpan);
    
    const btn = document.createElement('button');
    btn.classList.add('btn', 'btn-secondary');
    btn.innerHTML = '<i class="fas fa-cart-plus"></i> Añadir al carrito';
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