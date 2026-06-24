import { getUsuarios } from '../../../utils/api';
import { setCurrentUser } from '../../../utils/auth';
const form = document.getElementById('registerForm');
const errorDiv = document.getElementById('error');
form.addEventListener('submit', async (e) => {
    e.preventDefault();
    errorDiv.textContent = '';
    const nombre = document.getElementById('nombre').value.trim();
    const apellido = document.getElementById('apellido').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    if (!nombre || !apellido || !email || !password) {
        errorDiv.textContent = 'Completá todos los campos.';
        return;
    }
    if (password.length < 6) {
        errorDiv.textContent = 'La contraseña debe tener al menos 6 caracteres.';
        return;
    }
    const usuarios = await getUsuarios();
    if (usuarios.some(u => u.mail === email)) {
        errorDiv.textContent = 'Ya existe una cuenta con ese email.';
        return;
    }
    // En esta iteración solo se agrega al estado local (no se persiste en el JSON)
    const nuevoUsuario = {
        id: Date.now(),
        nombre,
        apellido,
        mail: email,
        rol: 'USUARIO',
    };
    // Auto-login tras registro exitoso
    setCurrentUser(nuevoUsuario);
    window.location.href = '../../store/home/index.html';
});
