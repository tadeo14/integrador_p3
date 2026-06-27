package com.tp.jpa.menu;

import com.tp.jpa.model.*;
import com.tp.jpa.model.enums.Estado;
import com.tp.jpa.model.enums.FormaPago;
import com.tp.jpa.repository.PedidoRepository;
import com.tp.jpa.repository.ProductoRepository;
import com.tp.jpa.repository.UsuarioRepository;
import com.tp.jpa.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.*;

public class PedidoMenu {

    private final PedidoRepository pedidoRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;
    private final Scanner scanner;

    public PedidoMenu(PedidoRepository pedidoRepo, ProductoRepository productoRepo,
                      UsuarioRepository usuarioRepo, Scanner scanner) {
        this.pedidoRepo = pedidoRepo;
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
        this.scanner = scanner;
    }

    public void mostrar() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- GESTION DE PEDIDOS ---");
            System.out.println("1. Alta de pedido");
            System.out.println("2. Cambiar estado");
            System.out.println("3. Baja logica");
            System.out.println("4. Listado");
            System.out.println("5. Pedidos por usuario");
            System.out.println("6. Pedidos por estado");
            System.out.println("0. Volver");
            System.out.print("Selecciona una opcion: ");
            String op = scanner.nextLine().trim();
            switch (op) {
                case "1" -> altaPedido();
                case "2" -> cambiarEstado();
                case "3" -> bajaLogica();
                case "4" -> listado();
                case "5" -> pedidosPorUsuario();
                case "6" -> pedidosPorEstado();
                case "0" -> volver = true;
                default  -> System.out.println("Opcion invalida.");
            }
        }
    }

    // ── 5.4.1 Alta de pedido: flujo completo en una unica transaccion ──────────
    private void altaPedido() {
        // 1. Seleccion de usuario
        List<Usuario> usuarios = usuarioRepo.listarActivos();
        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios activos. Crea un usuario primero.");
            return;
        }
        new UsuarioMenu(usuarioRepo, scanner).imprimirListado(usuarios);
        System.out.print("ID del usuario: ");
        Long idUsuario = parseLong(scanner.nextLine().trim());
        if (idUsuario == null) { System.out.println("ID invalido."); return; }
        Optional<Usuario> usuOpt = usuarioRepo.buscarPorId(idUsuario);
        if (usuOpt.isEmpty() || usuOpt.get().isEliminado()) {
            System.out.println("Usuario no encontrado o dado de baja.");
            return;
        }

        // 2. Forma de pago
        System.out.println("Forma de pago: 1. TARJETA  2. TRANSFERENCIA  3. EFECTIVO");
        System.out.print("Selecciona: ");
        FormaPago formaPago = switch (scanner.nextLine().trim()) {
            case "1" -> FormaPago.TARJETA;
            case "2" -> FormaPago.TRANSFERENCIA;
            case "3" -> FormaPago.EFECTIVO;
            default  -> null;
        };
        if (formaPago == null) { System.out.println("Forma de pago invalida."); return; }

        // 3. Carga de productos en lista temporal (solo IDs y cantidades)
        // La lista guarda idProducto → cantidad acumulada
        Map<Long, Integer> itemsTemporales = new LinkedHashMap<>();
        boolean agregarMas = true;
        while (agregarMas) {
            List<Producto> productos = productoRepo.listarActivos();
            if (productos.isEmpty()) {
                System.out.println("No hay productos activos.");
                break;
            }
            new ProductoMenu(productoRepo, null, scanner).imprimirListado(productos);
            System.out.print("ID del producto (0 para finalizar): ");
            Long idProd = parseLong(scanner.nextLine().trim());
            if (idProd == null || idProd == 0) break;

            Optional<Producto> prodOpt = productoRepo.buscarPorId(idProd);
            if (prodOpt.isEmpty() || prodOpt.get().isEliminado()) {
                System.out.println("Producto no encontrado o dado de baja.");
                continue;
            }
            Producto prod = prodOpt.get();
            if (!Boolean.TRUE.equals(prod.getDisponible())) {
                System.out.println("El producto no esta disponible.");
                continue;
            }
            System.out.print("Cantidad: ");
            Integer cantidad = parseInt(scanner.nextLine().trim());
            if (cantidad == null || cantidad <= 0) {
                System.out.println("Cantidad invalida (debe ser mayor a 0).");
                continue;
            }
            int yaReservado = itemsTemporales.getOrDefault(idProd, 0);
            if (prod.getStock() - yaReservado < cantidad) {
                System.out.println("Stock insuficiente. Disponible: " + (prod.getStock() - yaReservado));
                continue;
            }
            itemsTemporales.merge(idProd, cantidad, Integer::sum);
            System.out.println("Producto agregado: " + prod.getNombre() + " x" + cantidad);

            System.out.print("¿Agregar otro producto? (S/N): ");
            agregarMas = scanner.nextLine().trim().equalsIgnoreCase("S");
        }

        if (itemsTemporales.isEmpty()) {
            System.out.println("El pedido debe tener al menos un producto. Operacion cancelada.");
            return;
        }

        // 4. Unica transaccion para persistir todo
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            Usuario usuarioManaged = em.find(Usuario.class, idUsuario);

            Pedido pedido = Pedido.builder()
                    .fecha(LocalDate.now())
                    .estado(Estado.PENDIENTE)
                    .formaPago(formaPago)
                    .build();

            for (Map.Entry<Long, Integer> entry : itemsTemporales.entrySet()) {
                Producto prod = em.find(Producto.class, entry.getKey());
                int cantidad = entry.getValue();
                pedido.addDetallePedido(cantidad, prod);
                // Producto es managed → el cambio de stock se sincroniza en commit
                prod.setStock(prod.getStock() - cantidad);
            }

            pedido.calcularTotal();
            em.persist(pedido); // cascade ALL persiste los DetallePedido
            usuarioManaged.getPedidos().add(pedido); // establece el FK usuario_id

            em.getTransaction().commit();

            System.out.println("\n=== PEDIDO CREADO EXITOSAMENTE ===");
            System.out.println("ID:          " + pedido.getId());
            System.out.println("Fecha:       " + pedido.getFecha());
            System.out.println("Usuario:     " + usuarioManaged.getNombre() + " " + usuarioManaged.getApellido());
            System.out.println("Forma pago:  " + pedido.getFormaPago());
            System.out.println("--- Detalle ---");
            for (DetallePedido d : pedido.getDetalles()) {
                System.out.printf("  %-25s x%-3d $%.2f%n",
                        d.getProducto().getNombre(), d.getCantidad(), d.getSubtotal());
            }
            System.out.printf("TOTAL: $%.2f%n", pedido.getTotal());

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            System.out.println("Error al crear el pedido (rollback): " + e.getMessage());
        } finally {
            em.close();
        }
    }

    // ── Cambiar estado ─────────────────────────────────────────────────────────
    private void cambiarEstado() {
        System.out.print("ID del pedido: ");
        Long id = parseLong(scanner.nextLine().trim());
        if (id == null) { System.out.println("ID invalido."); return; }

        Optional<Pedido> opt = pedidoRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("Pedido no encontrado o dado de baja.");
            return;
        }
        Pedido pedido = opt.get();
        System.out.println("Estado actual: " + pedido.getEstado());
        System.out.println("1. PENDIENTE  2. CONFIRMADO  3. TERMINADO  4. CANCELADO");
        System.out.print("Nuevo estado: ");
        Estado nuevoEstado = switch (scanner.nextLine().trim()) {
            case "1" -> Estado.PENDIENTE;
            case "2" -> Estado.CONFIRMADO;
            case "3" -> Estado.TERMINADO;
            case "4" -> Estado.CANCELADO;
            default  -> null;
        };
        if (nuevoEstado == null) { System.out.println("Estado invalido."); return; }

        pedido.setEstado(nuevoEstado);
        pedidoRepo.guardar(pedido);
        System.out.println("Pedido #" + id + " → estado actualizado a: " + nuevoEstado);
    }

    // ── Baja logica ────────────────────────────────────────────────────────────
    private void bajaLogica() {
        System.out.print("ID del pedido a dar de baja: ");
        Long id = parseLong(scanner.nextLine().trim());
        if (id == null) { System.out.println("ID invalido."); return; }

        Optional<Pedido> opt = pedidoRepo.buscarPorId(id);
        Double total = opt.map(Pedido::getTotal).orElse(null);

        if (pedidoRepo.eliminarLogico(id)) {
            System.out.printf("Pedido #%d dado de baja. Total: $%.2f. El stock NO se restaura.%n",
                    id, total != null ? total : 0.0);
        } else {
            System.out.println("Error: no se encontro el pedido o ya estaba dado de baja.");
        }
    }

    // ── Listado ────────────────────────────────────────────────────────────────
    private void listado() {
        List<Pedido> pedidos = pedidoRepo.listarActivos();
        if (pedidos.isEmpty()) { System.out.println("No hay pedidos activos."); return; }
        System.out.println("\n--- PEDIDOS ACTIVOS ---");
        System.out.printf("%-5s %-12s %-12s %-15s %-25s %s%n",
                "ID", "Fecha", "Estado", "Forma pago", "Usuario", "Total");
        System.out.println("-".repeat(90));
        for (Pedido p : pedidos) {
            String nombreUsuario = usuarioRepo.buscarUsuarioDePedido(p.getId())
                    .map(u -> u.getNombre() + " " + u.getApellido())
                    .orElse("N/A");
            System.out.printf("%-5d %-12s %-12s %-15s %-25s $%.2f%n",
                    p.getId(), p.getFecha(), p.getEstado(), p.getFormaPago(),
                    nombreUsuario, p.getTotal() != null ? p.getTotal() : 0.0);
        }
    }

    // ── Pedidos por usuario ────────────────────────────────────────────────────
    private void pedidosPorUsuario() {
        List<Usuario> usuarios = usuarioRepo.listarActivos();
        if (usuarios.isEmpty()) { System.out.println("No hay usuarios activos."); return; }
        new UsuarioMenu(usuarioRepo, scanner).imprimirListado(usuarios);
        System.out.print("ID del usuario: ");
        Long id = parseLong(scanner.nextLine().trim());
        if (id == null) { System.out.println("ID invalido."); return; }

        List<Pedido> pedidos = usuarioRepo.buscarPedidosPorUsuario(id);
        if (pedidos.isEmpty()) {
            System.out.println("El usuario no tiene pedidos activos.");
            return;
        }
        System.out.println("\n--- PEDIDOS DEL USUARIO ---");
        System.out.printf("%-5s %-12s %-12s %-15s %s%n", "ID", "Fecha", "Estado", "Forma pago", "Total");
        System.out.println("-".repeat(60));
        for (Pedido p : pedidos) {
            System.out.printf("%-5d %-12s %-12s %-15s $%.2f%n",
                    p.getId(), p.getFecha(), p.getEstado(), p.getFormaPago(),
                    p.getTotal() != null ? p.getTotal() : 0.0);
        }
    }

    // ── Pedidos por estado ─────────────────────────────────────────────────────
    private void pedidosPorEstado() {
        System.out.println("Estados: 1. PENDIENTE  2. CONFIRMADO  3. TERMINADO  4. CANCELADO");
        System.out.print("Selecciona: ");
        Estado estado = switch (scanner.nextLine().trim()) {
            case "1" -> Estado.PENDIENTE;
            case "2" -> Estado.CONFIRMADO;
            case "3" -> Estado.TERMINADO;
            case "4" -> Estado.CANCELADO;
            default  -> null;
        };
        if (estado == null) { System.out.println("Estado invalido."); return; }

        List<Pedido> pedidos = pedidoRepo.buscarPorEstado(estado);
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos con estado: " + estado);
            return;
        }
        System.out.println("\n--- PEDIDOS CON ESTADO: " + estado + " ---");
        System.out.printf("%-5s %-12s %-25s %s%n", "ID", "Fecha", "Usuario", "Total");
        System.out.println("-".repeat(60));
        for (Pedido p : pedidos) {
            String nombreUsuario = usuarioRepo.buscarUsuarioDePedido(p.getId())
                    .map(u -> u.getNombre() + " " + u.getApellido())
                    .orElse("N/A");
            System.out.printf("%-5d %-12s %-25s $%.2f%n",
                    p.getId(), p.getFecha(), nombreUsuario,
                    p.getTotal() != null ? p.getTotal() : 0.0);
        }
    }

    private Long parseLong(String s) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }
}
