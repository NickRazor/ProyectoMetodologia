document.addEventListener('DOMContentLoaded', function() {
    // Mostrar modal de recuperación
    document.getElementById('recover-password-link').addEventListener('click', function(event) {
        event.preventDefault();
        document.getElementById('recoverPasswordModal').style.display = 'flex';
    });

    // Manejar el botón volver en el modal de recuperación
    document.querySelector('#recoverPasswordModal #back-to-login').addEventListener('click', function(event) {
        event.preventDefault();
        document.getElementById('recoverPasswordModal').style.display = 'none';
    });

    // Mostrar modal de registro
    document.getElementById('create-account').addEventListener('click', function(e) {
        e.preventDefault();
        document.getElementById('registerModal').style.display = 'flex';
    });

    // Cerrar modal de registro
    document.querySelector('#registerModal #back-to-login').addEventListener('click', function(e) {
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

    // Cerrar modales al hacer clic fuera
    window.onclick = function(event) {
        const recoverModal = document.getElementById('recoverPasswordModal');
        const registerModal = document.getElementById('registerModal');
        
        if (event.target === recoverModal) {
            recoverModal.style.display = 'none';
        }
        if (event.target === registerModal) {
            registerModal.style.display = 'none';
        }
    };

    // Manejar la visualización de contraseña
    document.querySelectorAll('.toggle-password').forEach(button => {
        button.addEventListener('click', function() {
            const input = this.parentElement.querySelector('input');
            const icon = this.querySelector('i');
            
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                input.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    });

    // Verificar fuerza de contraseña en tiempo real
    const passwordInput = document.getElementById('password');
    const strengthBar = document.querySelector('.strength-bar');
    const strengthText = document.querySelector('.strength-text');

    if (passwordInput) {
        passwordInput.addEventListener('input', function() {
            const strength = checkPasswordStrength(this.value);
            const strengthContainer = document.querySelector('.password-strength');
            
            strengthContainer.className = 'password-strength strength-' + strength.class;
            strengthText.textContent = 'Fuerza: ' + strength.text;
        });
    }
});

// Función para verificar la fuerza de la contraseña
function checkPasswordStrength(password) {
    let strength = 0;
    
    // Longitud mínima
    if (password.length >= 8) strength += 1;
    
    // Contiene números
    if (/\d/.test(password)) strength += 1;
    
    // Contiene letras minúsculas y mayúsculas
    if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength += 1;
    
    // Contiene caracteres especiales
    if (/[^A-Za-z0-9]/.test(password)) strength += 1;
    
    return {
        score: strength,
        text: strength < 2 ? 'Débil' : strength < 3 ? 'Media' : 'Fuerte',
        class: strength < 2 ? 'weak' : strength < 3 ? 'medium' : 'strong'
    };
}