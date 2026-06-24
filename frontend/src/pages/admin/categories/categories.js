import { getCategorias } from '../../../utils/api';
import { requireAuth, logout } from '../../../utils/auth';
requireAuth('ADMIN');
document.getElementById('logoutBtn').addEventListener('click', logout);
let categorias = [];
async function init() {
    categorias = await getCategorias();
    renderTable();
}
function renderTable() {
    const body = document.getElementById('catBody');
    const activas = categorias.filter(c => !c.eliminado);
    body.innerHTML = activas.map(c => `
    <tr>
      <td>${c.id}</td>
      <td><img src="${c.imagen || ''}" alt="" style="width:50px;height:50px;object-fit:cover;border-radius:4px" /></td>
      <td>${c.nombre}</td>
      <td>${c.descripcion}</td>
      <td>
        <button class="btn btn-warning btn-sm" data-action="edit" data-id="${c.id}">Editar</button>
        <button class="btn btn-danger btn-sm" data-action="del" data-id="${c.id}">Eliminar</button>
      </td>
    </tr>
  `).join('');
    body.querySelectorAll('button[data-action]').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = Number(btn.dataset['id']);
            const action = btn.dataset['action'];
            if (action === 'edit')
                openModal(id);
            else if (action === 'del')
                deleteCategoria(id);
        });
    });
}
function openModal(id) {
    const cat = id ? categorias.find(c => c.id === id) : null;
    document.getElementById('modalTitle').textContent = cat ? 'Editar categoría' : 'Nueva categoría';
    document.getElementById('editId').value = cat ? String(cat.id) : '';
    document.getElementById('nombre').value = cat?.nombre ?? '';
    document.getElementById('descripcion').value = cat?.descripcion ?? '';
    document.getElementById('imagen').value = cat?.imagen ?? '';
    document.getElementById('modal').classList.add('open');
}
function deleteCategoria(id) {
    const cat = categorias.find(c => c.id === id);
    if (!confirm(`¿Eliminar la categoría "${cat?.nombre}"?`))
        return;
    categorias = categorias.map(c => c.id === id ? { ...c, eliminado: true } : c);
    renderTable();
}
document.getElementById('newBtn').addEventListener('click', () => openModal());
document.getElementById('closeModal').addEventListener('click', () => document.getElementById('modal').classList.remove('open'));
document.getElementById('catForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const editId = document.getElementById('editId').value;
    const nombre = document.getElementById('nombre').value.trim();
    const descripcion = document.getElementById('descripcion').value.trim();
    const imagen = document.getElementById('imagen').value.trim();
    if (editId) {
        categorias = categorias.map(c => c.id === Number(editId) ? { ...c, nombre, descripcion, imagen } : c);
    }
    else {
        const newId = Math.max(0, ...categorias.map(c => c.id)) + 1;
        categorias.push({ id: newId, nombre, descripcion, imagen, eliminado: false });
    }
    document.getElementById('modal').classList.remove('open');
    renderTable();
});
init();
