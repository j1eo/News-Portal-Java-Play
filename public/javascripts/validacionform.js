// Validación del formulario de perfil
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('profileForm');
    
    if(form) {
        form.addEventListener('submit', function(e) {
            const urlImagen = document.getElementById('profileImageUrl').value;
            const email = document.getElementById('profileEmail').value;
            
            // Validación de imagen
            if(urlImagen && !urlImagen.match(/\.(jpe?g|png|gif|webp)(\?.*)?$/i)) {
                alert("La URL debe apuntar a una imagen (JPEG, PNG, GIF, WEBP)");
                e.preventDefault();
                return false;
            }
            
            // Validación de email
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(email)) {
                alert("Por favor ingrese un email válido");
                e.preventDefault();
                return false;
            }
        });
    }
});

// Función global para confirmar logout
window.confirmLogout = function() {
    return confirm("¿Estás seguro que deseas cerrar sesión?");
};