document.addEventListener('DOMContentLoaded', function() {
    // Mostrar modal de recuperación
    document.getElementById('recover-password-link').addEventListener('click', function(event) {
        event.preventDefault(); // Evita la recarga de la página
        document.getElementById('recoverPasswordModal').style.display = 'flex';  // Muestra el modal
    });

    // Volver al inicio de sesión
    document.getElementById('back-to-login').addEventListener('click', function(event) {
        // El enlace redirige a la página de login
        window.location.href = 'login.html';  // Reemplaza con la URL de tu página de inicio de sesión
    });

    window.onclick = function(event) {
        var modal = document.getElementById('recoverPasswordModal');
        if (event.target === modal) {
            modal.style.display = 'none';  // Cierra el modal si se hace clic fuera de él
        }
    };

    // Mostrar modal de registro
    document.getElementById('create-account').addEventListener('click', function(e) {
        e.preventDefault();
        document.getElementById('registerModal').style.display = 'flex';
    });

    // Cerrar modal de registro
    document.getElementById('back-to-login').addEventListener('click', function(e) {
        e.preventDefault();
        document.getElementById('registerModal').style.display = 'none';
    });

    // Validar contraseñas coincidentes
    document.getElementById('registerForm').addEventListener('submit', function(e) {
        const password = this.querySelector('input[name="password"]').value;
        const confirmarPassword = this.querySelector('input[name="confirmarPassword"]').value;
        
        if (password !== confirmarPassword) {
            e.preventDefault();
            alert('Las contraseñas no coinciden');
        }
    });

    // Ocultar mensaje de alerta después de 5 segundos
    const mensajeAlerta = document.getElementById('mensajeAlerta');
    if (mensajeAlerta) {
        setTimeout(() => {
            mensajeAlerta.style.display = 'none';
        }, 5000);
    }
});