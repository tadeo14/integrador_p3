import { requireAuth, logout } from '../../../utils/auth'
import { getCart, removeFromCart, updateQty, clearCart, ENVIO } from '../../../utils/cart'
import type { CartItem, Pedido } from '../../../types'

const user = requireAuth()
document.getElementById('logoutBtn')!.addEventListener('click', logout)

function render() {
  const cart: CartItem[] = getCart()
  const container = document.getElementById('cartContainer')!

  if (cart.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <h3>Tu carrito está vacío</h3>
        <p>¡Agregá productos desde el catálogo!</p>
        <a href="../home/index.html" class="btn btn-primary mt-2">Ir a la tienda</a>
      </div>`
    return
  }

  const subtotal = cart.reduce((s, i) => s + i.precio * i.cantidad, 0)
  const total    = subtotal + ENVIO

  container.innerHTML = `
    <div style="display:flex;gap:1.5rem;flex-wrap:wrap">
      <div style="flex:1;min-width:280px">
        ${cart.map(item => `
          <div class="cart-item">
            <img src="${item.imagen || 'https://via.placeholder.com/70'}" alt="${item.nombre}" />
            <div class="info">
              <strong>${item.nombre}</strong>
              <div>$${item.precio.toLocaleString()} c/u</div>
              <div class="qty-control mt-1">
                <button data-id="${item.idProducto}" data-action="dec">−</button>
                <span>${item.cantidad}</span>
                <button data-id="${item.idProducto}" data-action="inc">+</button>
              </div>
            </div>
            <div style="text-align:right">
              <div style="font-weight:700;color:var(--primary)">$${(item.precio * item.cantidad).toLocaleString()}</div>
              <button class="btn btn-danger btn-sm mt-1" data-id="${item.idProducto}" data-action="del">Eliminar</button>
            </div>
          </div>
        `).join('')}
      </div>
      <div class="summary-box" style="align-self:flex-start">
        <h3 class="mb-1">Resumen</h3>
        <div class="summary-row"><span>Subtotal</span><span>$${subtotal.toLocaleString()}</span></div>
        <div class="summary-row"><span>Envío</span><span>${ENVIO === 0 ? 'Gratis' : '$' + ENVIO}</span></div>
        <div class="summary-row summary-total"><span>Total</span><span>$${total.toLocaleString()}</span></div>
        <button id="checkoutBtn" class="btn btn-primary mt-2" style="width:100%">Proceder al pago</button>
      </div>
    </div>
  `

  container.querySelectorAll('button[data-action]').forEach(btn => {
    btn.addEventListener('click', () => {
      const id     = Number((btn as HTMLElement).dataset['id'])
      const action = (btn as HTMLElement).dataset['action']
      const item   = cart.find(i => i.idProducto === id)!
      if (action === 'del') removeFromCart(id)
      else if (action === 'inc' && item.cantidad < item.stock) updateQty(id, item.cantidad + 1)
      else if (action === 'dec' && item.cantidad > 1)          updateQty(id, item.cantidad - 1)
      render()
    })
  })

  document.getElementById('checkoutBtn')?.addEventListener('click', () => {
    fillOrderSummary(cart, subtotal, total)
    document.getElementById('checkoutModal')!.classList.add('open')
  })
}

function fillOrderSummary(cart: CartItem[], subtotal: number, total: number) {
  document.getElementById('orderSummary')!.innerHTML = `
    <div class="summary-box mt-2" style="max-width:100%">
      ${cart.map(i => `<div class="summary-row"><span>${i.nombre} x${i.cantidad}</span><span>$${(i.precio*i.cantidad).toLocaleString()}</span></div>`).join('')}
      <div class="summary-row"><span>Subtotal</span><span>$${subtotal.toLocaleString()}</span></div>
      <div class="summary-row"><span>Envío</span><span>${ENVIO === 0 ? 'Gratis' : '$'+ENVIO}</span></div>
      <div class="summary-row summary-total"><span>Total</span><span>$${total.toLocaleString()}</span></div>
    </div>`
}

document.getElementById('clearBtn')!.addEventListener('click', () => { clearCart(); render() })
document.getElementById('closeModal')!.addEventListener('click', () =>
  document.getElementById('checkoutModal')!.classList.remove('open'))

document.getElementById('checkoutForm')!.addEventListener('submit', (e) => {
  e.preventDefault()
  const formaPago = (document.getElementById('formaPago') as HTMLSelectElement).value as Pedido['formaPago']
  const cart = getCart()
  const subtotal = cart.reduce((s, i) => s + i.precio * i.cantidad, 0)

  const pedido: Pedido = {
    id:        Date.now(),
    fecha:     new Date().toISOString().split('T')[0],
    estado:    'PENDIENTE',
    total:     subtotal + ENVIO,
    formaPago,
    idUsuario: user.id,
    detalles:  cart.map(i => ({ idProducto: i.idProducto, cantidad: i.cantidad, subtotal: i.precio * i.cantidad }))
  }

  // Guardar pedido en localStorage (en esta iteración no se persiste en el JSON)
  const pedidos: Pedido[] = JSON.parse(localStorage.getItem('foodstore_pedidos') || '[]')
  pedidos.push(pedido)
  localStorage.setItem('foodstore_pedidos', JSON.stringify(pedidos))

  clearCart()
  alert('¡Pedido confirmado! Podés verlo en "Mis pedidos".')
  window.location.href = '../../client/orders/index.html'
})

render()
