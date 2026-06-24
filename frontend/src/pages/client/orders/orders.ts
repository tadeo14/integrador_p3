import { getPedidos, getProductos } from '../../../utils/api'
import { requireAuth, logout } from '../../../utils/auth'
import type { Pedido, Producto } from '../../../types'

const user = requireAuth('USUARIO')
document.getElementById('logoutBtn')!.addEventListener('click', logout)
document.getElementById('closeModal')!.addEventListener('click', () =>
  document.getElementById('detailModal')!.classList.remove('open'))

const BADGE: Record<string, string> = {
  PENDIENTE: 'badge-yellow', CONFIRMADO: 'badge-blue', TERMINADO: 'badge-green', CANCELADO: 'badge-red'
}

async function init() {
  const [jsonPedidos, productos] = await Promise.all([getPedidos(), getProductos()])

  // Combina pedidos del JSON con los generados en esta sesión (localStorage)
  const localPedidos: Pedido[] = JSON.parse(localStorage.getItem('foodstore_pedidos') || '[]')
  const allPedidos = [...jsonPedidos, ...localPedidos]

  const misPedidos = allPedidos.filter(p => p.idUsuario === user.id)
  const container  = document.getElementById('ordersList')!

  if (misPedidos.length === 0) {
    container.innerHTML = '<div class="empty-state"><h3>No tenés pedidos aún</h3><a href="../../store/home/index.html" class="btn btn-primary mt-2">Ir a la tienda</a></div>'
    return
  }

  container.innerHTML = misPedidos.map(p => {
    const primeros = p.detalles.slice(0, 3).map(d => {
      const prod = productos.find(x => x.id === d.idProducto)
      return prod ? `${prod.nombre} x${d.cantidad}` : `Producto #${d.idProducto}`
    }).join(', ')
    return `
      <div class="card mb-2" style="padding:1rem;cursor:pointer" data-id="${p.id}">
        <div class="flex-between">
          <div>
            <strong>Pedido #${p.id}</strong> &nbsp;
            <span class="badge ${BADGE[p.estado]}">${p.estado}</span>
          </div>
          <span style="color:#888;font-size:.9rem">${p.fecha}</span>
        </div>
        <p style="margin:.5rem 0;font-size:.9rem;color:#666">${primeros}${p.detalles.length > 3 ? '...' : ''}</p>
        <div class="text-right"><strong style="color:var(--primary)">$${p.total.toLocaleString()}</strong></div>
      </div>`
  }).join('')

  container.querySelectorAll('[data-id]').forEach(card => {
    card.addEventListener('click', () => {
      const id = Number((card as HTMLElement).dataset['id'])
      const pedido = misPedidos.find(p => p.id === id)!
      showDetail(pedido, productos)
    })
  })
}

function showDetail(pedido: Pedido, productos: Producto[]) {
  document.getElementById('modalContent')!.innerHTML = `
    <h2>Pedido #${pedido.id}</h2>
    <p><strong>Fecha:</strong> ${pedido.fecha}</p>
    <p><strong>Estado:</strong> <span class="badge ${BADGE[pedido.estado]}">${pedido.estado}</span></p>
    <p><strong>Forma de pago:</strong> ${pedido.formaPago}</p>
    <hr style="margin:1rem 0" />
    <h3>Productos</h3>
    ${pedido.detalles.map(d => {
      const prod = productos.find(x => x.id === d.idProducto)
      return `<div class="summary-row">
        <span>${prod?.nombre ?? 'Producto #'+d.idProducto} x${d.cantidad}</span>
        <span>$${d.subtotal.toLocaleString()}</span>
      </div>`
    }).join('')}
    <div class="summary-row summary-total"><span>Total</span><span>$${pedido.total.toLocaleString()}</span></div>
  `
  document.getElementById('detailModal')!.classList.add('open')
}

init()
