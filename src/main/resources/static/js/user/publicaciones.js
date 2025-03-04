function editarPublicacion(id) {
    if (!id) {
        console.error('ID de publicación no válido');
        return;
    }
    
    try {
        window.location.href = `/api/publicaciones/${id}/editar`;
    } catch (error) {
        console.error('Error al redirigir:', error);
        alert('Error al intentar editar la publicación');
    }
}

function eliminarPublicacion(id) {
    if (!id) {
        console.error('ID de publicación no válido');
        return;
    }

    if (confirm('¿Estás seguro de que deseas eliminar esta publicación?')) {
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
                response.text().then(text => alert('Error al eliminar: ' + text));
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error al eliminar la publicación');
        });
    }
}