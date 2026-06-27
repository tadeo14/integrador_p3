package com.tp.jpa.repository;

import com.tp.jpa.model.Pedido;
import com.tp.jpa.model.Usuario;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class UsuarioRepository extends BaseRepository<Usuario> {

    public UsuarioRepository() {
        super(Usuario.class);
    }

    /**
     * Busca un usuario activo por su direccion de correo electronico.
     * Retorna Optional para manejar el caso en que el mail no este registrado.
     */
    public Optional<Usuario> buscarPorMail(String mail) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT u FROM Usuario u WHERE u.mail = :mail AND u.eliminado = false";
            TypedQuery<Usuario> q = em.createQuery(jpql, Usuario.class);
            q.setParameter("mail", mail);
            List<Usuario> res = q.getResultList();
            return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Retorna el usuario activo dueno del pedido indicado.
     * Navega desde Usuario hacia su coleccion u.pedidos para encontrar
     * a que usuario pertenece un pedido (necesario para mostrar el nombre en listados).
     */
    public Optional<Usuario> buscarUsuarioDePedido(Long pedidoId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT u FROM Usuario u JOIN u.pedidos p WHERE p.id = :pid AND u.eliminado = false";
            List<Usuario> res = em.createQuery(jpql, Usuario.class)
                    .setParameter("pid", pedidoId)
                    .getResultList();
            return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Retorna los pedidos activos de un usuario dado.
     * Navega desde Usuario hacia su coleccion u.pedidos mediante JOIN.
     * Filtra por usuario.id = :uid y p.eliminado = false para excluir bajas logicas.
     */
    public List<Pedido> buscarPedidosPorUsuario(Long idUsuario) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT p FROM Usuario u JOIN u.pedidos p WHERE u.id = :uid AND p.eliminado = false";
            return em.createQuery(jpql, Pedido.class)
                    .setParameter("uid", idUsuario)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
