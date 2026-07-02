package com.tp.jpa.util;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.Pedido;
import com.tp.jpa.model.Producto;
import com.tp.jpa.model.Usuario;
import com.tp.jpa.model.enums.Estado;
import com.tp.jpa.model.enums.FormaPago;
import com.tp.jpa.model.enums.Rol;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;

/**
 * Carga inicial de datos (categorias, productos, usuarios y pedidos).
 * Espeja los datos de frontend/public/data/*.json para que ambas capas
 * trabajen con la misma informacion.
 *
 * Ejecutar con: gradlew seed
 * Es idempotente: si ya existen categorias en la base, no vuelve a cargar.
 */
public class DataSeeder {

    public static void main(String[] args) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            Long existentes = em.createQuery("SELECT COUNT(c) FROM Categoria c", Long.class)
                    .getSingleResult();
            if (existentes > 0) {
                System.out.println("La base ya tiene datos (" + existentes + " categorias). No se carga nada.");
                return;
            }

            em.getTransaction().begin();

            // ---------- Categorias ----------
            Categoria hamburguesas = Categoria.builder()
                    .nombre("Hamburguesas")
                    .descripcion("Las mejores hamburguesas artesanales")
                    .build();
            Categoria bebidas = Categoria.builder()
                    .nombre("Bebidas")
                    .descripcion("Gaseosas, jugos y agua")
                    .build();
            Categoria postres = Categoria.builder()
                    .nombre("Postres")
                    .descripcion("Dulces y tortas para el final")
                    .build();
            em.persist(hamburguesas);
            em.persist(bebidas);
            em.persist(postres);

            // ---------- Productos ----------
            Producto classicBurger = Producto.builder()
                    .nombre("Classic Burger")
                    .precio(1500.0)
                    .descripcion("Hamburguesa clasica con lechuga y tomate")
                    .stock(20)
                    .imagen("https://thespiceway.com/cdn/shop/files/Signature_Savory_Classic_Burger.jpg?v=1712161801")
                    .disponible(true)
                    .categoria(hamburguesas)
                    .build();
            Producto doubleCheese = Producto.builder()
                    .nombre("Double Cheese")
                    .precio(2000.0)
                    .descripcion("Doble carne con queso cheddar derretido")
                    .stock(15)
                    .imagen("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSLaRpz6uYMZs7KQxaO1eZEBtrAJ_EuGKiO6tabTCjZjw&s=10")
                    .disponible(true)
                    .categoria(hamburguesas)
                    .build();
            Producto veggieBurger = Producto.builder()
                    .nombre("Veggie Burger")
                    .precio(1800.0)
                    .descripcion("Hamburguesa de garbanzo y verduras")
                    .stock(10)
                    .imagen("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTSRFAR0ZEh_adTZNxFy_-TjxsepDCYSTkv4s9Gry9zTQ&s")
                    .disponible(true)
                    .categoria(hamburguesas)
                    .build();
            Producto cocaCola = Producto.builder()
                    .nombre("Coca-Cola 500ml")
                    .precio(500.0)
                    .descripcion("Gaseosa fria original")
                    .stock(50)
                    .imagen("https://viejodave.com.ar/wp-content/uploads/2024/02/categoria-bebidas.jpg")
                    .disponible(true)
                    .categoria(bebidas)
                    .build();
            Producto aguaMineral = Producto.builder()
                    .nombre("Agua mineral")
                    .precio(300.0)
                    .descripcion("Agua sin gas 500ml")
                    .stock(40)
                    .imagen("https://encrypted-tbn1.gstatic.com/shopping?q=tbn:ANd9GcQJULlqFDecVUz04mRu5xUwiBaitTROE7ArhxwNYGzYTBGrpbM5NVl_NEjJHCZ5-dcUtV0bZSyCKtDnDOyTyDDpKVhjCyWRlMxX8k2SQXihno_HukhPpSbmkWT89Dx000_7Of7vLUg&usqp=CAc")
                    .disponible(true)
                    .categoria(bebidas)
                    .build();
            Producto brownie = Producto.builder()
                    .nombre("Brownie de choco")
                    .precio(800.0)
                    .descripcion("Brownie humedo con chips de chocolate")
                    .stock(8)
                    .imagen("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRdfnErhRMemtaFB_rlwTb27WwPLASgTmd97WnIdBNlMQ&s=10")
                    .disponible(true)
                    .categoria(postres)
                    .build();
            Producto inactivo = Producto.builder()
                    .nombre("Producto inactivo")
                    .precio(100.0)
                    .descripcion("No debe mostrarse en el catalogo")
                    .stock(0)
                    .imagen("")
                    .disponible(false)
                    .categoria(hamburguesas)
                    .build();
            em.persist(classicBurger);
            em.persist(doubleCheese);
            em.persist(veggieBurger);
            em.persist(cocaCola);
            em.persist(aguaMineral);
            em.persist(brownie);
            em.persist(inactivo);

            // ---------- Usuarios ----------
            Usuario admin = Usuario.builder()
                    .nombre("Admin")
                    .apellido("Sistema")
                    .mail("admin@foodstore.com")
                    .celular("1112345678")
                    .contrasena("admin123")
                    .rol(Rol.ADMIN)
                    .build();
            Usuario juan = Usuario.builder()
                    .nombre("Juan")
                    .apellido("Perez")
                    .mail("juan@mail.com")
                    .celular("1198765432")
                    .contrasena("usuario123")
                    .rol(Rol.USUARIO)
                    .build();
            Usuario maria = Usuario.builder()
                    .nombre("Maria")
                    .apellido("Gonzalez")
                    .mail("maria@mail.com")
                    .celular("1155554444")
                    .contrasena("pass456")
                    .rol(Rol.USUARIO)
                    .build();

            // ---------- Pedidos (con detalles, asociados a su usuario) ----------
            Pedido pedido1 = Pedido.builder()
                    .fecha(LocalDate.of(2025, 6, 20))
                    .estado(Estado.TERMINADO)
                    .formaPago(FormaPago.TARJETA)
                    .build();
            pedido1.addDetallePedido(2, classicBurger);
            pedido1.addDetallePedido(2, cocaCola);
            pedido1.calcularTotal();
            juan.getPedidos().add(pedido1);

            Pedido pedido2 = Pedido.builder()
                    .fecha(LocalDate.of(2025, 6, 22))
                    .estado(Estado.PENDIENTE)
                    .formaPago(FormaPago.EFECTIVO)
                    .build();
            pedido2.addDetallePedido(1, doubleCheese);
            pedido2.addDetallePedido(1, aguaMineral);
            pedido2.calcularTotal();
            maria.getPedidos().add(pedido2);

            Pedido pedido3 = Pedido.builder()
                    .fecha(LocalDate.of(2025, 6, 23))
                    .estado(Estado.CONFIRMADO)
                    .formaPago(FormaPago.TRANSFERENCIA)
                    .build();
            pedido3.addDetallePedido(1, veggieBurger);
            pedido3.calcularTotal();
            juan.getPedidos().add(pedido3);

            // persistir usuarios cascadea pedidos y detalles (CascadeType.ALL)
            em.persist(admin);
            em.persist(juan);
            em.persist(maria);

            em.getTransaction().commit();

            System.out.println("Carga inicial completada:");
            System.out.println("  - 3 categorias");
            System.out.println("  - 7 productos");
            System.out.println("  - 3 usuarios");
            System.out.println("  - 3 pedidos (con sus detalles)");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
            JPAUtil.close();
        }
    }
}
