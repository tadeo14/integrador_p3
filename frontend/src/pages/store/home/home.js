import { getCategorias, getProductos } from '../../../utils/api';
import { requireAuth, logout } from '../../../utils/auth';
import { cartCount } from '../../../utils/cart';
const user = requireAuth();
document.getElementById('userName').textContent = `Hola, ${user.nombre}`;
document.getElementById('logoutBtn').addEventListener('click', logout);
document.getElementById('cartCount').textContent = String(cartCount());
let allProductos = [];
let categorias = [];
let selectedCatId = null;
async function init() {
    [categorias, allProductos] = await Promise.all([getCategorias(), getProductos()]);
    // Solo categorías activas
    const catActivas = categorias.filter(c => !c.eliminado);
    const catList = document.getElementById('catList');
    catList.innerHTML = '<li class="active" data-id="all">Todas</li>';
    catActivas.forEach(c => {
        const li = document.createElement('li');
        li.textContent = c.nombre;
        li.dataset['id'] = String(c.id);
        li.addEventListener('click', () => {
            selectedCatId = c.id;
            document.querySelectorAll('.cat-sidebar li').forEach(el => el.classList.remove('active'));
            li.classList.add('active');
            renderGrid();
        });
        catList.appendChild(li);
    });
    catList.querySelector('[data-id="all"]').addEventListener('click', () => {
        selectedCatId = null;
        document.querySelectorAll('.cat-sidebar li').forEach(el => el.classList.remove('active'));
        catList.querySelector('[data-id="all"]').classList.add('active');
        renderGrid();
    });
    renderGrid();
}
function renderGrid() {
    const search = document.getElementById('searchInput').value.toLowerCase();
    const sort = document.getElementById('sortSelect').value;
    let productos = allProductos
        .filter(p => p.disponible && !p.eliminado)
        .filter(p => selectedCatId == null || p.categoriaId === selectedCatId)
        .filter(p => p.nombre.toLowerCase().includes(search));
    if (sort === 'nombre-asc')
        productos.sort((a, b) => a.nombre.localeCompare(b.nombre));
    if (sort === 'nombre-desc')
        productos.sort((a, b) => b.nombre.localeCompare(a.nombre));
    if (sort === 'precio-asc')
        productos.sort((a, b) => a.precio - b.precio);
    if (sort === 'precio-desc')
        productos.sort((a, b) => b.precio - a.precio);
    const grid = document.getElementById('productGrid');
    if (productos.length === 0) {
        grid.innerHTML = '<div class="empty-state"><h3>No hay productos disponibles</h3></div>';
        return;
    }
    grid.innerHTML = productos.map(p => `
    <div class="card product-card" data-id="${p.id}">
      <img src="${p.imagen || 'https://via.placeholder.com/300/ccc/999?text=Producto'}" alt="${p.nombre}" />
      <div class="info">
        <h3>${p.nombre}</h3>
        <p style="font-size:.85rem;color:#666;margin:.3rem 0">${p.descripcion}</p>
        <div class="flex-between mt-1">
          <span class="price">$${p.precio.toLocaleString()}</span>
          <span class="badge ${p.disponible ? 'badge-green' : 'badge-red'}">
            ${p.disponible ? 'Disponible' : 'No disponible'}
          </span>
        </div>
      </div>
    </div>
  `).join('');
    grid.querySelectorAll('.product-card').forEach(card => {
        card.addEventListener('click', () => {
            const id = card.dataset['id'];
            window.location.href = `../productDetail/index.html?id=${id}`;
        });
    });
}
document.getElementById('searchInput').addEventListener('input', renderGrid);
document.getElementById('sortSelect').addEventListener('change', renderGrid);
init();
