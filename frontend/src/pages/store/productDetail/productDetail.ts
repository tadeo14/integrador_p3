import { getProductos, getCategorias } from '../../../utils/api'
import { requireAuth, logout } from '../../../utils/auth'
import { addToCart, cartCount } from '../../../utils/cart'

requireAuth()
document.getElementById('logoutBtn')!.addEventListener('click', logout)
document.getElementById('cartCount')!.textContent = String(cartCount())

const params = new URLSearchParams(location.search)
const id = Number(params.get('id'))

async function init() {
  const [productos, categorias] = await Promise.all([getProductos(), getCategorias()])
  const p = productos.find(x => x.id === id)
  const container = document.getElementById('productDetail')!

  if (!p) {
    container.innerHTML = '<div class="empty-state"><h3>Producto no encontrado</h3></div>'
    return
  }

  const cat = categorias.find(c => c.id === p.categoriaId)
  const disponible = p.disponible && !p.eliminado && p.stock > 0

  container.innerHTML = `
    <div class="card" style="display:flex;gap:1.5rem;padding:1.5rem">
      <img src="${p.imagen || 'https://placehold.co/300x200/cccccc/999999?text=Imagen'}" alt="${p.nombre}"
           style="width:280px;height:280px;object-fit:cover;border-radius:8px;flex-shrink:0" />
      <div style="flex:1">
        <h1 style="margin-bottom:.5rem">${p.nombre}</h1>
        <p style="color:#666;margin-bottom:1rem">${p.descripcion}</p>
        <p style="font-size:.9rem;color:#888">Categoría: ${cat?.nombre ?? 'N/A'}</p>
        <p class="price" style="font-size:1.6rem;margin:1rem 0">$${p.precio.toLocaleString()}</p>
        <p>Stock disponible: <strong>${p.stock}</strong></p>
        <span class="badge ${disponible ? 'badge-green' : 'badge-red'}" style="margin:.5rem 0;display:inline-block">
          ${disponible ? 'Disponible' : 'No disponible'}
        </span>

        ${disponible ? `
          <div class="d-flex gap-2 mt-2" style="align-items:center">
            <label><strong>Cantidad:</strong></label>
            <input type="number" id="qty" value="1" min="1" max="${p.stock}"
                   style="width:70px;padding:.4rem;border:1px solid #ddd;border-radius:6px" />
          </div>
          <button id="addBtn" class="btn btn-primary mt-2">Agregar al carrito</button>
          <div id="confirm" style="color:green;margin-top:.5rem;font-weight:600"></div>
        ` : '<p class="auth-error mt-2">Este producto no está disponible para compra.</p>'}
      </div>
    </div>
  `

  if (disponible) {
    document.getElementById('addBtn')!.addEventListener('click', () => {
      const qty = Number((document.getElementById('qty') as HTMLInputElement).value)
      if (qty < 1 || qty > p.stock) {
        document.getElementById('confirm')!.textContent = `Cantidad inválida (máx. ${p.stock})`
        document.getElementById('confirm')!.style.color = 'red'
        return
      }
      addToCart({ idProducto: p.id, nombre: p.nombre, precio: p.precio, cantidad: qty, imagen: p.imagen, stock: p.stock })
      document.getElementById('confirm')!.textContent = '✓ Producto agregado al carrito'
      document.getElementById('confirm')!.style.color = 'green'
      document.getElementById('cartCount')!.textContent = String(cartCount())
    })
  }
}

init()
