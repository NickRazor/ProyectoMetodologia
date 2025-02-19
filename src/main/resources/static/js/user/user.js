document.addEventListener('DOMContentLoaded', function() {
    const mensajeAlerta = document.getElementById('mensajeAlerta');
    if (mensajeAlerta) {
        setTimeout(() => {
            mensajeAlerta.style.display = 'none';
        }, 5000); // El mensaje desaparecerá después de 5 segundos
    }
});