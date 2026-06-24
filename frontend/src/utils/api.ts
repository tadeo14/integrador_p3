/**
 * Capa de fetch aislada. Para conectar la API REST en la próxima iteración,
 * basta reemplazar cada URL '/data/X.json' por el endpoint correspondiente
 * (ej: '/api/categorias'). La firma de cada función no cambia.
 */
import type { Categoria, Producto, Usuario, Pedido } from '../types'

export const getCategorias = (): Promise<Categoria[]> =>
  fetch('/data/categorias.json').then(r => r.json())

export const getProductos = (): Promise<Producto[]> =>
  fetch('/data/productos.json').then(r => r.json())

export const getUsuarios = (): Promise<Usuario[]> =>
  fetch('/data/usuarios.json').then(r => r.json())

export const getPedidos = (): Promise<Pedido[]> =>
  fetch('/data/pedidos.json').then(r => r.json())
