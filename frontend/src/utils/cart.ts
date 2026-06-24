import type { CartItem } from '../types'

/** Costo de envío fijo (documentado en README). */
export const ENVIO = 0

const KEY = 'foodstore_cart'

export function getCart(): CartItem[] {
  const raw = localStorage.getItem(KEY)
  return raw ? JSON.parse(raw) : []
}

function saveCart(cart: CartItem[]): void {
  localStorage.setItem(KEY, JSON.stringify(cart))
}

export function addToCart(item: CartItem): void {
  const cart = getCart()
  const existing = cart.find(i => i.idProducto === item.idProducto)
  if (existing) {
    existing.cantidad += item.cantidad
  } else {
    cart.push({ ...item })
  }
  saveCart(cart)
}

export function removeFromCart(idProducto: number): void {
  saveCart(getCart().filter(i => i.idProducto !== idProducto))
}

export function updateQty(idProducto: number, cantidad: number): void {
  const cart = getCart()
  const item = cart.find(i => i.idProducto === idProducto)
  if (item) {
    item.cantidad = cantidad
    saveCart(cart)
  }
}

export function clearCart(): void {
  localStorage.removeItem(KEY)
}

export function cartCount(): number {
  return getCart().reduce((acc, i) => acc + i.cantidad, 0)
}
