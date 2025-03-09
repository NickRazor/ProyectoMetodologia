document.addEventListener('DOMContentLoaded', function() {
    // Vista previa del título
    const tituloInput = document.getElementById('titulo');
    if (tituloInput) {
        tituloInput.addEventListener('input', actualizarVistaPrevia);
    }

    // Vista previa de la categoría
    const categoriaSelect = document.getElementById('categoria');
    if (categoriaSelect) {
        categoriaSelect.addEventListener('change', actualizarVistaPrevia);
    }

    // Vista previa del precio
    const precioInput = document.getElementById('precio');
    if (precioInput) {
        precioInput.addEventListener('input', actualizarVistaPrevia);
    }

    // Vista previa de la descripción
    const descripcionInput = document.getElementById('descripcion');
    if (descripcionInput) {
        descripcionInput.addEventListener('input', actualizarVistaPrevia);
    }

    // Vista previa de la imagen
    const archivoInput = document.getElementById('archivo');
    if (archivoInput) {
        archivoInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            const previewImage = document.getElementById('preview-image');
            
            if (file) {
                // Validar tipo de archivo
                if (!file.type.startsWith('image/')) {
                    alert('Por favor, selecciona un archivo de imagen válido');
                    archivoInput.value = '';
                    return;
                }

                const reader = new FileReader();
                reader.onload = function(e) {
                    previewImage.src = e.target.result;
                    previewImage.style.display = 'block';
                }
                reader.onerror = function(e) {
                    console.error('Error al leer el archivo:', e);
                    alert('Error al cargar la imagen');
                }
                reader.readAsDataURL(file);
            } else if (previewImage.dataset.defaultSrc) {
                previewImage.src = previewImage.dataset.defaultSrc;
            } else {
                previewImage.src = '';
                previewImage.style.display = 'none';
            }
        });
    }

    // Actualizar fecha y hora en tiempo real
    const fechaPreview = document.getElementById('preview-fecha');
    if (fechaPreview) {
        const actualizarFecha = () => {
            const fecha = new Date();
            const formatoFecha = fecha.toLocaleString('es-ES', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
            fechaPreview.textContent = `Fecha: ${formatoFecha}`;
        };
        actualizarFecha();
    }

    // Vista previa del ID amigable
    const previewId = document.getElementById('preview-id');
    if (previewId) {
        // Obtener el ID amigable del atributo data si existe
        const publicacionId = previewId.dataset.friendlyId || generarIdAmigable();
        previewId.textContent = `Ref: ${publicacionId}`;
    }
});

// Función para actualizar la vista previa en tiempo real
function actualizarVistaPrevia() {
    const previewTitulo = document.getElementById('preview-titulo');
    const previewCategoria = document.getElementById('preview-categoria');
    const previewPrecio = document.getElementById('preview-precio');
    const previewDescripcion = document.getElementById('preview-descripcion');

    // Actualizar título
    if (previewTitulo) {
        const tituloInput = document.getElementById('titulo');
        previewTitulo.textContent = tituloInput.value || 'Nombre del artículo';
    }

    // Actualizar categoría
    if (previewCategoria) {
        const categoriaSelect = document.getElementById('categoria');
        const categoriasNombres = {
            'electronics': 'Electrónicos',
            'home': 'Casa y Jardín',
            'toys': 'Juguetes',
            'watches': 'Relojes',
            '': 'Sin categoría'
        };
        
        console.log('Categoría seleccionada:', categoriaSelect.value); // Debug
        const nombreCategoria = categoriasNombres[categoriaSelect.value] || 'Sin categoría';
        previewCategoria.textContent = `Categoría: ${nombreCategoria}`;
    }

    // Actualizar precio
    if (previewPrecio) {
        const precioInput = document.getElementById('precio');
        const precio = parseFloat(precioInput.value) || 0;
        previewPrecio.textContent = `Precio $ ${precio.toFixed(2)}`;
    }

    // Actualizar descripción
    if (previewDescripcion) {
        const descripcionInput = document.getElementById('descripcion');
        previewDescripcion.textContent = descripcionInput.value || 'Descripción:';
    }
}

// Funciones para editar y eliminar publicaciones
function editarPublicacion(id) {
    if (!id) {
        console.error('ID de publicación no válido');
        return;
    }
    window.location.href = `/api/publicaciones/${id}/editar`;
}

function eliminarPublicacion(id) {
    if (!id || !confirm('¿Estás seguro de que deseas eliminar esta publicación?')) {
        return;
    }

    fetch(`/api/publicaciones/${id}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            window.location.reload();
        } else {
            return response.text().then(text => {
                throw new Error(text || 'Error al eliminar la publicación');
            });
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert(error.message);
    });
}

// Función para generar un ID amigable
function generarIdAmigable() {
    const fecha = new Date();
    const timestamp = fecha.getTime();
    const random = Math.floor(Math.random() * 1000);
    return `PROD-${timestamp}-${random}`;
}