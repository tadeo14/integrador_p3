import { getCategorias, getProductos, getPedidos, getUsuarios } from '../../../utils/api';
import { requireAuth, logout } from '../../../utils/auth';
requireAuth('ADMIN');
document.getElementById('logoutBtn').addEventListener('click', logout);
async function init() {
    const [cats, prods, pedidos, usuarios] = await Promise.all([
        getCategorias(), getProductos(), getPedidos(), getUsuarios()
    ]);
    const activeCats = cats.filter(c => !c.eliminado);
    const activeProds = prods.filter(p => !p.eliminado);
    const disponibles = activeProds.filter(p => p.disponible);
    const inactivos = activeProds.filter(p => !p.disponible);
    document.getElementById('stats').innerHTML = `
    <div class="stat-card"><div class="num">${activeCats.length}</div><div class="label">Categorías</div></div>
    <div class="stat-card"><div class="num">${activeProds.length}</div><div class="label">Productos</div></div>
    <div class="stat-card"><div class="num">${pedidos.length}</div><div class="label">Pedidos</div></div>
    <div class="stat-card"><div class="num">${disponibles.length}</div><div class="label">Disponibles</div></div>
  `;
    const byEstado = ['PENDIENTE', 'CONFIRMADO', 'TERMINADO', 'CANCELADO'].map(e => `<span class="badge ${e === 'PENDIENTE' ? 'badge-yellow' : e === 'CONFIRMADO' ? 'badge-blue' : e === 'TERMINADO' ? 'badge-green' : 'badge-red'}">${e}: ${pedidos.filter(p => p.estado === e).length}</span>`).join(' ');
    document.getElementById('summary').innerHTML = `
    <div class="card card-body">
      <h3 class="mb-1">Resumen</h3>
      <p>Productos activos: ${disponibles.length} disponibles / ${inactivos.length} no disponibles</p>
      <p style="margin-top:.5rem">Pedidos por estado: ${byEstado}</p>
      <p style="margin-top:.5rem">Usuarios registrados: ${usuarios.length}</p>
    </div>`;
}
init();
