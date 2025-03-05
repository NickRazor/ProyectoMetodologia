document.addEventListener('DOMContentLoaded', function() {
    // Toggle sidebar
    const menuButton = document.getElementById('menuButton');
    const sidebar = document.querySelector('.sidebar');
    
    if (menuButton && sidebar) {
        menuButton.addEventListener('click', function() {
            sidebar.classList.toggle('active');
        });

        // Cerrar sidebar al hacer clic fuera
        document.addEventListener('click', function(event) {
            if (!event.target.closest('.sidebar') && 
                !event.target.closest('#menuButton') && 
                window.innerWidth <= 768 &&
                sidebar.classList.contains('active')) {
                sidebar.classList.remove('active');
            }
        });
    }

    // Manejo de alertas
    const mensajeAlerta = document.getElementById('mensajeAlerta');
    if (mensajeAlerta) {
        setTimeout(() => {
            mensajeAlerta.style.display = 'none';
        }, 5000);
    }

    // Vista previa del título
    document.getElementById('titulo').addEventListener('input', function(e) {
        document.getElementById('preview-titulo').textContent = e.target.value || 'Nombre del artículo';
    });

    // Vista previa de la categoría
    document.getElementById('categoria').addEventListener('change', function(e) {
        const categoriaSeleccionada = e.target.value;
        const categoriaMostrada = document.getElementById('preview-categoria');
        
        // Mapeo de valores a nombres más amigables
        const categoriasNombres = {
            'electronics': 'Electrónicos',
            'home': 'Casa y Jardín',
            'toys': 'Juguetes',
            'watches': 'Relojes',
            'clothing': 'Ropa'
        };

        if (categoriaMostrada) {
            categoriaMostrada.textContent = categoriaSeleccionada ? 
                `Categoría: ${categoriasNombres[categoriaSeleccionada] || categoriaSeleccionada}` : 
                'Categoría: Sin seleccionar';
        }
    });

    // Vista previa del precio
    document.getElementById('precio').addEventListener('input', function(e) {
        document.getElementById('preview-precio').textContent = `Precio $ ${e.target.value || '0.00'}`;
    });

    // Vista previa de la descripción
    document.getElementById('descripcion').addEventListener('input', function(e) {
        document.getElementById('preview-descripcion').textContent = e.target.value || 'Descripción:';
    });

    // Vista previa de la imagen
    document.getElementById('archivo').addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                document.getElementById('preview-image').src = e.target.result;
            }
            reader.readAsDataURL(file);
        }
    });
});

function editarPublicacion(id) {
    if (!id) return;
    console.log('Editando publicación:', id);
    // Implementar lógica de edición
}

function eliminarPublicacion(id) {
    if (!id) return;
    if (confirm('¿Estás seguro de que deseas eliminar esta publicación?')) {
        fetch(`/api/publicaciones/${id}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.ok) {
                location.reload();
            } else {
                alert('Error al eliminar la publicación');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error al eliminar la publicación');
        });
    }
}