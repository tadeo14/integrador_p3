export const getCategorias = () => fetch('/data/categorias.json').then(r => r.json());
export const getProductos = () => fetch('/data/productos.json').then(r => r.json());
export const getUsuarios = () => fetch('/data/usuarios.json').then(r => r.json());
export const getPedidos = () => fetch('/data/pedidos.json').then(r => r.json());
