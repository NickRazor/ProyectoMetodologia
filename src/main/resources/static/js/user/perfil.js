document.addEventListener('DOMContentLoaded', function() {
    // Ocultar el mensaje de alerta después de 5 segundos
    const mensajeAlerta = document.getElementById('mensajeAlerta');
    if (mensajeAlerta) {
        setTimeout(function() {
            mensajeAlerta.style.opacity = '0';
            setTimeout(function() {
                mensajeAlerta.style.display = 'none';
            }, 500);
        }, 5000);
    }
    
    // Función para la confirmación de eliminación de cuenta
    window.confirmarEliminacion = function() {
        const confirmacion = confirm("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.");
        if (confirmacion) {
            // Enviar solicitud para eliminar la cuenta
            fetch('/user/eliminar-cuenta', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include'
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert("Tu cuenta ha sido eliminada exitosamente");
                    window.location.href = '/';
                } else {
                    alert("Error al eliminar la cuenta: " + data.mensaje);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert("Ocurrió un error al procesar tu solicitud");
            });
        }
    };
});