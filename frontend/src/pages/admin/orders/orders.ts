import { getPedidos, getUsuarios, getProductos } from '../../../utils/api'
import { requireAuth, logout } from '../../../utils/auth'
import type { Pedido, Usuario, Producto } from '../../../types'

requireAuth('ADMIN')
document.getElementById('logoutBtn')!.addEventListener('click', logout)
document.getElementById('closeModal')!.addEventListener('click', () =>
  document.getElementById('modal')!.classList.remove('open'))

const BADGE: Record<string, string> = {
  PENDIENTE: 'badge-yellow', CONFIRMADO: 'badge-blue', TERMINADO: 'badge-green', CANCELADO: 'badge-red'
}

let pedidos: Pedido[]   = []
let usuarios: Usuario[] = []
let productos: Producto[] = []

async function init() {
  ;[pedidos, usuarios, productos] = await Promise.all([getPedidos(), getUsuarios(), getProductos()])
  // Ordenar por fecha descendente
  pedidos.sort((a, b) => b.fecha.localeCompare(a.fecha))
  renderList()
}

function renderList() {
  const estado = (document.getElementById('estadoFilter') as HTMLSelectElement).value
  const filtered = estado ? pedidos.filter(p => p.estado === estado) : pedidos
  const container = document.getElementById('ordersList')!

  if (filtered.length === 0) {
    container.innerHTML = '<div class="empty-state"><h3>No hay pedidos</h3></div>'
    return
  }

  container.innerHTML = filtered.map(p => {
    const usr = usuarios.find(u => u.id === p.idUsuario)
    return `
      <div class="card mb-1" style="padding:1rem;cursor:pointer" data-id="${p.id}">
        <div class="flex-between">
          <div>
            <strong>Pedido #${p.id}</strong> &nbsp;
            <span class="badge ${BADGE[p.estado]}">${p.estado}</span>
          </div>
          <span style="color:#888;font-size:.9rem">${p.fecha}</span>
        </div>
        <div class="flex-between mt-1">
          <span>${usr ? usr.nombre + ' ' + usr.apellido : 'Usuario #'+p.idUsuario}</span>
          <strong style="color:var(--primary)">$${p.total.toLocaleString()}</strong>
        </div>
        <small style="color:#888">${p.detalles.length} producto(s) · ${p.formaPago}</small>
      </div>`
  }).join('')

  container.querySelectorAll('[data-id]').forEach(card => {
    card.addEventListener('click', () => {
      const id = Number((card as HTMLElement).dataset['id'])
      const pedido = pedidos.find(p => p.id === id)!
      showDetail(pedido)
    })
  })
}

function showDetail(pedido: Pedido) {
  const usr = usuarios.find(u => u.id === pedido.idUsuario)
  document.getElementById('modalContent')!.innerHTML = `
    <h2>Pedido #${pedido.id}</h2>
    <p><strong>Cliente:</strong> ${usr ? usr.nombre + ' ' + usr.apellido : 'N/A'}</p>
    <p><strong>Fecha:</strong> ${pedido.fecha}</p>
    <p><strong>Forma de pago:</strong> ${pedido.formaPago}</p>
    <div class="form-group mt-2">
      <label><strong>Estado:</strong></label>
      <select id="estadoSelect">
        ${['PENDIENTE','CONFIRMADO','TERMINADO','CANCELADO'].map(e =>
          `<option value="${e}" ${e===pedido.estado?'selected':''}>${e}</option>`
        ).join('')}
      </select>
      <button id="saveEstado" class="btn btn-primary btn-sm mt-1">Guardar estado</button>
    </div>
    <hr style="margin:1rem 0"/>
    <h3>Productos</h3>
    ${pedido.detalles.map(d => {
      const prod = productos.find(x => x.id === d.idProducto)
      return `<div class="summary-row">
        <span>${prod?.nombre ?? '#'+d.idProducto} x${d.cantidad}</span>
        <span>$${d.subtotal.toLocaleString()}</span>
      </div>`
    }).join('')}
    <div class="summary-row summary-total"><span>Total</span><span>$${pedido.total.toLocaleString()}</span></div>
  `
  document.getElementById('modal')!.classList.add('open')

  document.getElementById('saveEstado')!.addEventListener('click', () => {
    const nuevoEstado = (document.getElementById('estadoSelect') as HTMLSelectElement).value as Pedido['estado']
    pedidos = pedidos.map(p => p.id === pedido.id ? { ...p, estado: nuevoEstado } : p)
    document.getElementById('modal')!.classList.remove('open')
    renderList()
  })
}

document.getElementById('estadoFilter')!.addEventListener('change', renderList)
init()
