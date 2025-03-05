class CarritoStorage {
    static guardarCarrito(items) {
        localStorage.setItem('carrito', JSON.stringify(items));
    }

    static obtenerCarrito() {
        return JSON.parse(localStorage.getItem('carrito')) || [];
    }

    static agregarItem(item) {
        const carrito = this.obtenerCarrito();
        const itemExistente = carrito.find(i => i.productoId === item.productoId);
        
        if (itemExistente) {
            itemExistente.cantidad += item.cantidad;
        } else {
            carrito.push(item);
        }
        
        this.guardarCarrito(carrito);
        this.actualizarUI();
    }

    static eliminarItem(productoId) {
        let carrito = this.obtenerCarrito();
        carrito = carrito.filter(item => item.productoId !== productoId);
        this.guardarCarrito(carrito);
        this.actualizarUI();
    }

    static actualizarCantidad(productoId, cantidad) {
        const carrito = this.obtenerCarrito();
        const item = carrito.find(i => i.productoId === productoId);
        if (item) {
            item.cantidad = cantidad;
            this.guardarCarrito(carrito);
            this.actualizarUI();
        }
    }

    static actualizarUI() {
        const carrito = this.obtenerCarrito();
        const contador = document.getElementById('cartCount');
        if (contador) {
            const totalItems = carrito.reduce((acc, item) => acc + item.cantidad, 0);
            contador.textContent = totalItems.toString();
        }
    }

    static sincronizarConServidor() {
        const carrito = this.obtenerCarrito();
        if (carrito.length > 0) {
            CarritoService.obtenerCarrito().then(carritoServidor => {
                // Sincronizar items locales con servidor
                carrito.forEach(item => {
                    const itemServidor = carritoServidor.items.find(i => i.productoId === item.productoId);
                    if (!itemServidor) {
                        CarritoService.agregarAlCarrito(item.productoId, item.cantidad);
                    }
                });
            });
        }
    }
}

// Sincronizar al cargar la página
document.addEventListener('DOMContentLoaded', () => {
    CarritoStorage.sincronizarConServidor();
});