import type { Usuario } from '../types'

const KEY = 'foodstore_user'

export function getCurrentUser(): Omit<Usuario, 'password'> | null {
  const raw = localStorage.getItem(KEY)
  return raw ? JSON.parse(raw) : null
}

export function setCurrentUser(user: Omit<Usuario, 'password'>): void {
  localStorage.setItem(KEY, JSON.stringify(user))
}

export function logout(): void {
  localStorage.removeItem(KEY)
  window.location.href = '/src/pages/auth/login/index.html'
}

/** Redirige al login si no hay sesión. Opcionalmente valida el rol. */
export function requireAuth(role?: 'ADMIN' | 'USUARIO'): Omit<Usuario, 'password'> {
  const user = getCurrentUser()
  if (!user) {
    window.location.href = '/src/pages/auth/login/index.html'
    throw new Error('Sin sesión')
  }
  if (role && user.rol !== role) {
    alert('Acceso denegado.')
    history.back()
    throw new Error('Rol insuficiente')
  }
  return user
}
