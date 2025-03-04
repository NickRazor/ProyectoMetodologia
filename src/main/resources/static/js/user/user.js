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