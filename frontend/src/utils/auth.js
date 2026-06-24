const KEY = 'foodstore_user';
export function getCurrentUser() {
    const raw = localStorage.getItem(KEY);
    return raw ? JSON.parse(raw) : null;
}
export function setCurrentUser(user) {
    localStorage.setItem(KEY, JSON.stringify(user));
}
export function logout() {
    localStorage.removeItem(KEY);
    window.location.href = '/src/pages/auth/login/index.html';
}
/** Redirige al login si no hay sesión. Opcionalmente valida el rol. */
export function requireAuth(role) {
    const user = getCurrentUser();
    if (!user) {
        window.location.href = '/src/pages/auth/login/index.html';
        throw new Error('Sin sesión');
    }
    if (role && user.rol !== role) {
        alert('Acceso denegado.');
        history.back();
        throw new Error('Rol insuficiente');
    }
    return user;
}
