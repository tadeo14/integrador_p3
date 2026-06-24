package com.tp.jpa.menu;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.Pedido;
import com.tp.jpa.model.Producto;
import com.tp.jpa.model.Usuario;
import com.tp.jpa.model.enums.Estado;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.PedidoRepository;
import com.tp.jpa.repository.UsuarioRepository;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ReporteMenu {

    private final CategoriaRepository categoriaRepo;
    private final PedidoRepository pedidoRepo;
    private final UsuarioRepository usuarioRepo;
    private final Scanner scanner;

    public ReporteMenu(CategoriaRepository categoriaRepo, PedidoRepository pedidoRepo,
                       UsuarioRepository usuarioRepo, Scanner scanner) {
        this.categoriaRepo = categoriaRepo;
        this.pedidoRepo = pedidoRepo;
        this.usuarioRepo = usuarioRepo;
        this.scanner = scanner;
    }

    public void mostrar() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- REPORTES ---");
            System.out.println("1. Productos por categoría");
            System.out.println("2. Pedidos por usuario");
            System.out.println("3. Pedidos por estado");
            System.out.println("4. Total facturado");
            System.out.println("0. Volver");
            System.out.print("Seleccioná una opción: ");
            String op = scanner.nextLine().trim();
            switch (op) {
                case "1" -> productosPorCategoria();
                case "2" -> pedidosPorUsuario();
                case "3" -> pedidosPorEstado();
                case "4" -> totalFacturado();
                case "0" -> volver = true;
                default  -> System.out.println("Opción inválida.");
            }
        }
    }

    private void productosPorCategoria() {
        List<Categoria> categorias = categoriaRepo.listarActivos();
        if (categorias.isEmpty()) { System.out.println("No hay categorías activas."); return; }
        new CategoriaMenu(categoriaRepo, scanner).imprimirListado(categorias);
        System.out.print("ID de la categoría: ");
        Long id = parseLong(scanner.nextLine().trim());
        if (id == null) { System.out.println("ID inválido."); return; }

        List<Producto> productos = categoriaRepo.buscarProductosPorCategoria(id);
        if (productos.isEmpty()) {
            System.out.println("La categoría no tiene productos activos.");
            return;
        }
        System.out.println("\n--- PRODUCTOS DE LA CATEGORÍA ---");
        System.out.printf("%-5s %-25s %-10s %s%n", "ID", "Nombre", "Precio", "Stock");
        System.out.println("-".repeat(50));
        for (Producto p : productos) {
            System.out.printf("%-5d %-25s %-10.2f %d%n",
                    p.getId(), p.getNombre(), p.getPrecio(), p.getStock());
        }
    }

    private void pedidosPorUsuario() {
        List<Usuario> usuarios = usuarioRepo.listarActivos();
        if (usuarios.isEmpty()) { System.out.println("No hay usuarios activos."); return; }
        new UsuarioMenu(usuarioRepo, scanner).imprimirListado(usuarios);
        System.out.print("ID del usuario: ");
        Long id = parseLong(scanner.nextLine().trim());
        if (id == null) { System.out.println("ID inválido."); return; }

        List<Pedido> pedidos = usuarioRepo.buscarPedidosPorUsuario(id);
        if (pedidos.isEmpty()) {
            System.out.println("El usuario no tiene pedidos activos.");
            return;
        }
        System.out.println("\n--- PEDIDOS DEL USUARIO ---");
        System.out.printf("%-5s %-12s %-12s %-15s %s%n",
                "ID", "Fecha", "Estado", "Forma pago", "Total");
        System.out.println("-".repeat(60));
        for (Pedido p : pedidos) {
            System.out.printf("%-5d %-12s %-12s %-15s $%.2f%n",
                    p.getId(), p.getFecha(), p.getEstado(), p.getFormaPago(),
                    p.getTotal() != null ? p.getTotal() : 0.0);
        }
    }

    private void pedidosPorEstado() {
        System.out.println("Estados: 1. PENDIENTE  2. CONFIRMADO  3. TERMINADO  4. CANCELADO");
        System.out.print("Seleccioná: ");
        Estado estado = switch (scanner.nextLine().trim()) {
            case "1" -> Estado.PENDIENTE;
            case "2" -> Estado.CONFIRMADO;
            case "3" -> Estado.TERMINADO;
            case "4" -> Estado.CANCELADO;
            default  -> null;
        };
        if (estado == null) { System.out.println("Estado inválido."); return; }

        List<Pedido> pedidos = pedidoRepo.buscarPorEstado(estado);
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos con estado: " + estado);
            return;
        }
        System.out.println("\n--- PEDIDOS CON ESTADO: " + estado + " ---");
        System.out.printf("%-5s %-12s %-25s %s%n", "ID", "Fecha", "Usuario", "Total");
        System.out.println("-".repeat(60));
        for (Pedido p : pedidos) {
            String usuario = usuarioRepo.buscarUsuarioDePedido(p.getId())
                    .map(u -> u.getNombre() + " " + u.getApellido())
                    .orElse("N/A");
            System.out.printf("%-5d %-12s %-25s $%.2f%n",
                    p.getId(), p.getFecha(), usuario,
                    p.getTotal() != null ? p.getTotal() : 0.0);
        }
    }

    private void totalFacturado() {
        List<Pedido> terminados = pedidoRepo.buscarPorEstado(Estado.TERMINADO);
        double total = terminados.stream()
                .mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0.0)
                .sum();
        System.out.println("Total facturado: " + String.format(Locale.US, "$%.2f", total));
    }

    private Long parseLong(String s) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }
}
