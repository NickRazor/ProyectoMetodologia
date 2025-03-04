function confirmarEliminacion() {
    if (confirm('¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.')) {
        fetch('/user/perfil/eliminar', { 
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                window.location.href = '/auth/login?mensaje=cuenta-eliminada';
            } else {
                response.text().then(text => alert('Error al eliminar la cuenta: ' + text));
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error al eliminar la cuenta');
        });
    }
}