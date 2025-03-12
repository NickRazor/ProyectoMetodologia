document.addEventListener("DOMContentLoaded", function() {
    // Datos de productos por categoría (en producción se cargarán desde el servidor)
    const products = {
        telefonos: [
            { id: "phone1", name: "iPhone 13", price: 999.99, image: "/img/productos/iphone13.jpg", rating: 4.5, stock: 50 },
            { id: "phone2", name: "Samsung Galaxy S21", price: 899.99, image: "/img/productos/galaxys21.jpg", rating: 4.7, stock: 30 },
            { id: "phone3", name: "Google Pixel 6", price: 799.99, image: "/img/productos/pixel6.jpg", rating: 4.6, stock: 40 },
            { id: "phone4", name: "OnePlus 9 Pro", price: 899.99, image: "/img/productos/oneplus9.jpg", rating: 4.4, stock: 25 },
            { id: "phone5", name: "Xiaomi Mi 11", price: 699.99, image: "/img/productos/mi11.jpg", rating: 4.3, stock: 35 }
        ],
        pc: [
            { id: "pc1", name: "MacBook Pro", price: 1999.99, image: "/img/productos/macbook.jpg", rating: 4.8, stock: 20 },
            { id: "pc2", name: "Dell XPS 13", price: 1499.99, image: "/img/productos/dellxps.jpg", rating: 4.7, stock: 15 },
            { id: "pc3", name: "HP Spectre x360", price: 1299.99, image: "/img/productos/hpspectre.jpg", rating: 4.6, stock: 18 },
            { id: "pc4", name: "Lenovo ThinkPad X1", price: 1599.99, image: "/img/productos/thinkpad.jpg", rating: 4.5, stock: 22 },
            { id: "pc5", name: "Asus ROG Zephyrus", price: 1799.99, image: "/img/productos/zephyrus.jpg", rating: 4.4, stock: 10 }
        ],
        smartwatch: [
            { id: "watch1", name: "Apple Watch Series 7", price: 399.99, image: "/img/productos/applewatch.jpg", rating: 4.6, stock: 40 },
            { id: "watch2", name: "Samsung Galaxy Watch 4", price: 299.99, image: "/img/productos/galaxywatch.jpg", rating: 4.5, stock: 35 },
            { id: "watch3", name: "Fitbit Sense", price: 299.99, image: "/img/productos/fitbit.jpg", rating: 4.4, stock: 30 },
            { id: "watch4", name: "Garmin Venu 2", price: 349.99, image: "/img/productos/garmin.jpg", rating: 4.7, stock: 25 },
            { id: "watch5", name: "Huawei Watch GT 3", price: 249.99, image: "/img/productos/huawei.jpg", rating: 4.3, stock: 20 }
        ],
        camaras: [
            { id: "cam1", name: "Canon EOS R5", price: 3499.99, image: "/img/productos/canonr5.jpg", rating: 4.9, stock: 10 },
            { id: "cam2", name: "Sony A7 III", price: 1999.99, image: "/img/productos/sonya7.jpg", rating: 4.8, stock: 12 },
            { id: "cam3", name: "Nikon Z6", price: 1799.99, image: "/img/productos/nikonz6.jpg", rating: 4.7, stock: 15 },
            { id: "cam4", name: "Fujifilm X-T4", price: 1699.99, image: "/img/productos/fujifilm.jpg", rating: 4.6, stock: 18 },
            { id: "cam5", name: "Panasonic Lumix S5", price: 1999.99, image: "/img/productos/lumix.jpg", rating: 4.5, stock: 20 }
        ],
        audifonos: [
            { id: "audio1", name: "Sony WH-1000XM4", price: 349.99, image: "/img/productos/sonyxm4.jpg", rating: 4.7, stock: 50 },
            { id: "audio2", name: "Bose QuietComfort 45", price: 329.99, image: "/img/productos/boseqc45.jpg", rating: 4.6, stock: 45 },
            { id: "audio3", name: "AirPods Max", price: 549.99, image: "/img/productos/airpodsmax.jpg", rating: 4.8, stock: 30 },
            { id: "audio4", name: "Sennheiser Momentum 3", price: 399.99, image: "/img/productos/sennheiser.jpg", rating: 4.5, stock: 25 },
            { id: "audio5", name: "Jabra Elite 85h", price: 299.99, image: "/img/productos/jabra.jpg", rating: 4.4, stock: 35 }
        ],
        videojuegos: [
            { id: "game1", name: "PlayStation 5", price: 499.99, image: "/img/productos/ps5.jpg", rating: 4.9, stock: 15 },
            { id: "game2", name: "Xbox Series X", price: 499.99, image: "/img/productos/xboxseriesx.jpg", rating: 4.8, stock: 12 },
            { id: "game3", name: "Nintendo Switch", price: 299.99, image: "/img/productos/switch.jpg", rating: 4.7, stock: 20 },
            { id: "game4", name: "Oculus Quest 2", price: 299.99, image: "/img/productos/oculus.jpg", rating: 4.6, stock: 18 },
            { id: "game5", name: "Steam Deck", price: 399.99, image: "/img/productos/steamdeck.jpg", rating: 4.5, stock: 10 }
        ]
    };

    // Función para mostrar productos de una categoría
    window.showCategory = function(category) {
        const productsGrid = document.getElementById('products-grid');
        const viewAllButton = document.getElementById('view-all-button');
        productsGrid.innerHTML = ''; // Limpiar productos actuales

        if (products[category]) {
            products[category].forEach(product => {
                productsGrid.innerHTML += `
                    <div class="product-card">
                        <img src="${product.image}" alt="${product.name}" onerror="this.src='/img/productos/default.jpg'">
                        <h3>${product.name}</h3>
                        <p class="price">US $${product.price.toFixed(2)}</p>
                        <div class="rating">
                            ${generateStarRating(product.rating)}
                        </div>
                        <p class="stock">${product.stock} disponibles</p>
                        <button onclick="addToCartFromExplore('${product.id}', '${product.name}', ${product.price}, '${product.image}')">Agregar al carrito</button>
                    </div>
                `;
            });
        } else {
            productsGrid.innerHTML = '<p>No hay productos disponibles en esta categoría.</p>';
        }

        // Ocultar el botón "Ver todos los productos" al seleccionar una categoría
        viewAllButton.style.display = 'none';
    };

    // Función para generar estrellas de calificación
    function generateStarRating(rating) {
        let stars = '';
        for (let i = 1; i <= 5; i++) {
            if (i <= Math.floor(rating)) {
                stars += '<i class="fas fa-star"></i>'; // Estrella llena
            } else if (i === Math.ceil(rating) && rating % 1 !== 0) {
                stars += '<i class="fas fa-star-half-alt"></i>'; // Media estrella
            } else {
                stars += '<i class="far fa-star"></i>'; // Estrella vacía
            }
        }
        return stars;
    }

    // Función para mostrar 15 productos al azar al inicio
    function showRandomProducts() {
        const productsGrid = document.getElementById('products-grid');
        const allProducts = [];

        // Combinar todos los productos en un solo array
        for (const category in products) {
            allProducts.push(...products[category]);
        }

        // Seleccionar 15 productos al azar o menos si no hay suficientes
        const productCount = Math.min(15, allProducts.length);
        const randomProducts = allProducts.sort(() => 0.5 - Math.random()).slice(0, productCount);

        // Limpiar el indicador de carga
        productsGrid.innerHTML = '';

        // Mostrar los productos seleccionados
        randomProducts.forEach(product => {
            productsGrid.innerHTML += `
                <div class="product-card">
                    <img src="${product.image}" alt="${product.name}" onerror="this.src='/img/productos/default.jpg'">
                    <h3>${product.name}</h3>
                    <p class="price">US $${product.price.toFixed(2)}</p>
                    <div class="rating">
                        ${generateStarRating(product.rating)}
                    </div>
                    <p class="stock">${product.stock} disponibles</p>
                    <button onclick="addToCartFromExplore('${product.id}', '${product.name}', ${product.price}, '${product.image}')">Agregar al carrito</button>
                </div>
            `;
        });

        // Mostrar el botón "Ver todos los productos"
        document.getElementById('view-all-button').style.display = 'block';
    }

    // Función para mostrar todos los productos
    window.showAllProducts = function() {
        const productsGrid = document.getElementById('products-grid');
        productsGrid.innerHTML = ''; // Limpiar productos actuales

        for (const category in products) {
            products[category].forEach(product => {
                productsGrid.innerHTML += `
                    <div class="product-card">
                        <img src="${product.image}" alt="${product.name}" onerror="this.src='/img/productos/default.jpg'">
                        <h3>${product.name}</h3>
                        <p class="price">US $${product.price.toFixed(2)}</p>
                        <div class="rating">
                            ${generateStarRating(product.rating)}
                        </div>
                        <p class="stock">${product.stock} disponibles</p>
                        <button onclick="addToCartFromExplore('${product.id}', '${product.name}', ${product.price}, '${product.image}')">Agregar al carrito</button>
                    </div>
                `;
            });
        }
    };

    // Mostrar productos al azar al cargar la página
    showRandomProducts();
});

// Función global para agregar un producto al carrito
function addToCartFromExplore(productId, productName, productPrice, productImage) {
    try {
        if (window.CarritoService) {
            // Preparar el item para el carrito
            const item = {
                id: productId,
                titulo: productName,
                precioUnitario: productPrice,
                imagen: productImage,
                cantidad: 1
            };
            
            // Usar el servicio de carrito existente
            CarritoService.agregarAlCarrito(item)
                .then(() => {
                    // Mostrar mensaje de éxito
                    const notification = document.createElement('div');
                    notification.className = 'add-to-cart-notification';
                    notification.innerHTML = `
                        <div class="notification-content">
                            <i class="fas fa-check-circle"></i>
                            <p>${productName} agregado al carrito</p>
                        </div>
                    `;
                    document.body.appendChild(notification);
                    
                    // Eliminar notificación después de 3 segundos
                    setTimeout(() => {
                        notification.remove();
                    }, 3000);
                    
                    // Actualizar contador del carrito
                    if (typeof CarritoService.actualizarContadorCarrito === 'function') {
                        CarritoService.actualizarContadorCarrito();
                    }
                })
                .catch(error => {
                    console.error('Error al agregar al carrito:', error);
                    alert('Error al agregar el producto al carrito. Por favor, inténtalo de nuevo.');
                });
        } else {
            // Fallback en caso de que CarritoService no esté disponible
            alert(`${productName} agregado al carrito.`);
            console.warn('CarritoService no está disponible');
        }
    } catch (error) {
        console.error('Error al agregar al carrito:', error);
        alert('Error al agregar el producto al carrito.');
    }
}

// Estilos para la notificación de agregar al carrito (se agregan dinámicamente)
document.addEventListener('DOMContentLoaded', function() {
    const style = document.createElement('style');
    style.textContent = `
        .add-to-cart-notification {
            position: fixed;
            bottom: 20px;
            right: 20px;
            background-color: #4CAF50;
            color: white;
            padding: 15px 20px;
            border-radius: 5px;
            box-shadow: 0 3px 10px rgba(0, 0, 0, 0.2);
            z-index: 1000;
            animation: slideInRight 0.3s ease-out, fadeOut 0.5s ease-out 2.5s;
        }

        .notification-content {
            display: flex;
            align-items: center;
        }

        .notification-content i {
            margin-right: 10px;
            font-size: 20px;
        }

        @keyframes slideInRight {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }

        @keyframes fadeOut {
            from { opacity: 1; }
            to { opacity: 0; }
        }
    `;
    document.head.appendChild(style);
});