import { getUsuarios } from '../../../utils/api';
import { setCurrentUser, getCurrentUser } from '../../../utils/auth';
// Si ya hay sesión, redirigir
const existing = getCurrentUser();
if (existing) {
    window.location.href = existing.rol === 'ADMIN'
        ? '../../admin/adminHome/index.html'
        : '../../store/home/index.html';
}
const form = document.getElementById('loginForm');
const errorDiv = document.getElementById('error');
form.addEventListener('submit', async (e) => {
    e.preventDefault();
    errorDiv.textContent = '';
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    if (!email || !password) {
        errorDiv.textContent = 'Completá todos los campos.';
        return;
    }
    const usuarios = await getUsuarios();
    const usuario = usuarios.find(u => u.mail === email && u.password === password);
    if (!usuario) {
        errorDiv.textContent = 'Credenciales incorrectas.';
        return;
    }
    const { password: _pw, ...userSinPassword } = usuario;
    setCurrentUser(userSinPassword);
    window.location.href = usuario.rol === 'ADMIN'
        ? '../../admin/adminHome/index.html'
        : '../../store/home/index.html';
});
