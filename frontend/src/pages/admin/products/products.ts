import { getProductos, getCategorias } from '../../../utils/api'
import { requireAuth, logout } from '../../../utils/auth'
import type { Producto, Categoria } from '../../../types'

requireAuth('ADMIN')
document.getElementById('logoutBtn')!.addEventListener('click', logout)

let productos: Producto[]  = []
let categorias: Categoria[] = []

async function init() {
  ;[productos, categorias] = await Promise.all([getProductos(), getCategorias()])
  populateCatSelect()
  renderTable()
}

function populateCatSelect() {
  const sel = document.getElementById('categoriaId') as HTMLSelectElement
  sel.innerHTML = categorias.filter(c => !c.eliminado).map(c =>
    `<option value="${c.id}">${c.nombre}</option>`
  ).join('')
}

function renderTable() {
  const body = document.getElementById('prodBody')!
  const activos = productos.filter(p => !p.eliminado)
  body.innerHTML = activos.map(p => {
    const cat = categorias.find(c => c.id === p.categoriaId)
    return `<tr>
      <td>${p.id}</td>
      <td><img src="${p.imagen||''}" alt="" style="width:50px;height:50px;object-fit:cover;border-radius:4px"/></td>
      <td>${p.nombre}</td>
      <td style="max-width:150px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${p.descripcion}</td>
      <td>$${p.precio.toLocaleString()}</td>
      <td>${cat?.nombre ?? 'N/A'}</td>
      <td>${p.stock}</td>
      <td><span class="badge ${p.disponible ? 'badge-green' : 'badge-red'}">${p.disponible ? 'Activo' : 'Inactivo'}</span></td>
      <td>
        <button class="btn btn-warning btn-sm" data-action="edit" data-id="${p.id}">Editar</button>
        <button class="btn btn-danger btn-sm" data-action="del" data-id="${p.id}">Eliminar</button>
      </td>
    </tr>`
  }).join('')

  body.querySelectorAll('button[data-action]').forEach(btn => {
    btn.addEventListener('click', () => {
      const id     = Number((btn as HTMLElement).dataset['id'])
      const action = (btn as HTMLElement).dataset['action']
      if (action === 'edit') openModal(id)
      else deleteProducto(id)
    })
  })
}

function openModal(id?: number) {
  const p = id ? productos.find(x => x.id === id) : null
  document.getElementById('modalTitle')!.textContent = p ? 'Editar producto' : 'Nuevo producto'
  ;(document.getElementById('editId')      as HTMLInputElement).value  = p ? String(p.id) : ''
  ;(document.getElementById('nombre')      as HTMLInputElement).value  = p?.nombre ?? ''
  ;(document.getElementById('descripcion') as HTMLInputElement).value  = p?.descripcion ?? ''
  ;(document.getElementById('precio')      as HTMLInputElement).value  = p ? String(p.precio) : ''
  ;(document.getElementById('stock')       as HTMLInputElement).value  = p ? String(p.stock) : ''
  ;(document.getElementById('imagen')      as HTMLInputElement).value  = p?.imagen ?? ''
  ;(document.getElementById('categoriaId') as HTMLSelectElement).value = p ? String(p.categoriaId) : ''
  ;(document.getElementById('disponible')  as HTMLSelectElement).value = p ? String(p.disponible) : 'true'
  document.getElementById('modal')!.classList.add('open')
}

function deleteProducto(id: number) {
  const p = productos.find(x => x.id === id)
  if (!confirm(`¿Eliminar "${p?.nombre}"?`)) return
  productos = productos.map(x => x.id === id ? { ...x, eliminado: true } : x)
  renderTable()
}

document.getElementById('newBtn')!.addEventListener('click', () => openModal())
document.getElementById('closeModal')!.addEventListener('click', () =>
  document.getElementById('modal')!.classList.remove('open'))

document.getElementById('prodForm')!.addEventListener('submit', (e) => {
  e.preventDefault()
  const editId     = (document.getElementById('editId')      as HTMLInputElement).value
  const nombre     = (document.getElementById('nombre')      as HTMLInputElement).value.trim()
  const descripcion= (document.getElementById('descripcion') as HTMLInputElement).value.trim()
  const precio     = Number((document.getElementById('precio') as HTMLInputElement).value)
  const stock      = Number((document.getElementById('stock')  as HTMLInputElement).value)
  const categoriaId= Number((document.getElementById('categoriaId') as HTMLSelectElement).value)
  const imagen     = (document.getElementById('imagen')      as HTMLInputElement).value.trim()
  const disponible = (document.getElementById('disponible')  as HTMLSelectElement).value === 'true'

  if (precio <= 0) { alert('El precio debe ser mayor a 0.'); return }
  if (stock < 0)   { alert('El stock no puede ser negativo.'); return }
  if (!categoriaId){ alert('Seleccioná una categoría.'); return }

  if (editId) {
    productos = productos.map(p => p.id === Number(editId)
      ? { ...p, nombre, descripcion, precio, stock, categoriaId, imagen, disponible }
      : p)
  } else {
    const newId = Math.max(0, ...productos.map(p => p.id)) + 1
    productos.push({ id: newId, nombre, descripcion, precio, stock, categoriaId, imagen, disponible, eliminado: false })
  }
  document.getElementById('modal')!.classList.remove('open')
  renderTable()
})

init()
