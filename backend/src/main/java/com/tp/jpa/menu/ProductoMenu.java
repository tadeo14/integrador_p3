package com.tp.jpa.menu;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.Producto;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.ProductoRepository;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ProductoMenu {

    private final ProductoRepository productoRepo;
    private final CategoriaRepository categoriaRepo;
    private final Scanner scanner;

    public ProductoMenu(ProductoRepository productoRepo, CategoriaRepository categoriaRepo, Scanner scanner) {
        this.productoRepo = productoRepo;
        this.categoriaRepo = categoriaRepo;
        this.scanner = scanner;
    }

    public void mostrar() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- GESTION DE PRODUCTOS ---");
            System.out.println("1. Alta");
            System.out.println("2. Modificar");
            System.out.println("3. Baja logica");
            System.out.println("4. Listado");
            System.out.println("0. Volver");
            System.out.print("Selecciona una opcion: ");
            String op = scanner.nextLine().trim();
            switch (op) {
                case "1" -> alta();
                case "2" -> modificar();
                case "3" -> bajaLogica();
                case "4" -> listado();
                case "0" -> volver = true;
                default  -> System.out.println("Opcion invalida.");
            }
        }
    }

    private void alta() {
        List<Categoria> categorias = categoriaRepo.listarActivos();
        if (categorias.isEmpty()) {
            System.out.println("No hay categorias activas. Crea una categoria primero.");
            return;
        }
        new CategoriaMenu(categoriaRepo, scanner).imprimirListado(categorias);
        System.out.print("ID de la categoria: ");
        Long catId = CategoriaMenu.parseLong(scanner.nextLine().trim());
        if (catId == null) { System.out.println("ID invalido."); return; }
        Optional<Categoria> catOpt = categoriaRepo.buscarPorId(catId);
        if (catOpt.isEmpty() || catOpt.get().isEliminado()) {
            System.out.println("Categoria no encontrada o dada de baja.");
            return;
        }
        Categoria categoria = catOpt.get();

        System.out.print("Nombre (obligatorio): ");
        String nombre = scanner.nextLine().trim();
        if (nombre.isEmpty()) { System.out.println("Error: el nombre es obligatorio."); return; }

        System.out.print("Descripcion (opcional): ");
        String descripcion = scanner.nextLine().trim();

        System.out.print("Precio (mayor a 0): ");
        Double precio = parseDouble(scanner.nextLine().trim());
        if (precio == null || precio <= 0) { System.out.println("Precio invalido."); return; }

        System.out.print("Stock (mayor o igual a 0): ");
        Integer stock = parseInt(scanner.nextLine().trim());
        if (stock == null || stock < 0) { System.out.println("Stock invalido."); return; }

        System.out.print("Imagen (URL o nombre de archivo, opcional): ");
        String imagen = scanner.nextLine().trim();

        System.out.print("¿Disponible? (S/N, Enter = S): ");
        String dispStr = scanner.nextLine().trim();
        boolean disponible = !dispStr.equalsIgnoreCase("N");

        Producto prod = Producto.builder()
                .nombre(nombre)
                .descripcion(descripcion.isEmpty() ? null : descripcion)
                .precio(precio)
                .stock(stock)
                .imagen(imagen.isEmpty() ? null : imagen)
                .disponible(disponible)
                .categoria(categoria)
                .build();
        Producto guardado = productoRepo.guardar(prod);
        System.out.println("Producto creado con ID: " + guardado.getId()
                + " — Categoria: " + categoria.getNombre());
    }

    private void modificar() {
        List<Producto> activos = productoRepo.listarActivos();
        if (activos.isEmpty()) { System.out.println("No hay productos activos."); return; }
        imprimirListado(activos);

        System.out.print("ID a modificar: ");
        Long id = CategoriaMenu.parseLong(scanner.nextLine().trim());
        if (id == null) { System.out.println("ID invalido."); return; }

        Optional<Producto> opt = productoRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("Producto no encontrado o dado de baja.");
            return;
        }
        Producto prod = opt.get();

        System.out.println("Nombre actual: " + prod.getNombre());
        System.out.print("Nuevo nombre (Enter para conservar): ");
        String nombre = scanner.nextLine().trim();
        if (!nombre.isEmpty()) prod.setNombre(nombre);

        System.out.println("Precio actual: " + prod.getPrecio());
        System.out.print("Nuevo precio (Enter para conservar): ");
        String precioStr = scanner.nextLine().trim();
        if (!precioStr.isEmpty()) {
            Double precio = parseDouble(precioStr);
            if (precio == null || precio <= 0) { System.out.println("Precio invalido, no se modifico."); }
            else prod.setPrecio(precio);
        }

        System.out.println("Stock actual: " + prod.getStock());
        System.out.print("Nuevo stock (Enter para conservar): ");
        String stockStr = scanner.nextLine().trim();
        if (!stockStr.isEmpty()) {
            Integer stock = parseInt(stockStr);
            if (stock == null || stock < 0) { System.out.println("Stock invalido, no se modifico."); }
            else prod.setStock(stock);
        }

        productoRepo.guardar(prod);
        System.out.println("Producto actualizado correctamente.");
    }

    private void bajaLogica() {
        System.out.print("ID del producto a dar de baja: ");
        Long id = CategoriaMenu.parseLong(scanner.nextLine().trim());
        if (id == null) { System.out.println("ID invalido."); return; }

        Optional<Producto> opt = productoRepo.buscarPorId(id);
        String nombre = opt.map(Producto::getNombre).orElse("(desconocido)");

        if (productoRepo.eliminarLogico(id)) {
            System.out.println("Producto \"" + nombre + "\" dado de baja exitosamente.");
        } else {
            System.out.println("Error: no se encontro el producto o ya estaba dado de baja.");
        }
    }

    private void listado() {
        List<Producto> activos = productoRepo.listarActivos();
        if (activos.isEmpty()) { System.out.println("No hay productos activos."); return; }
        System.out.println("\n--- PRODUCTOS ACTIVOS ---");
        imprimirListado(activos);
    }

    public void imprimirListado(List<Producto> lista) {
        System.out.printf("%-5s %-25s %-10s %-6s %-12s %s%n",
                "ID", "Nombre", "Precio", "Stock", "Disponible", "Categoria");
        System.out.println("-".repeat(75));
        for (Producto p : lista) {
            System.out.printf("%-5d %-25s %-10.2f %-6d %-12s %s%n",
                    p.getId(), p.getNombre(), p.getPrecio(), p.getStock(),
                    Boolean.TRUE.equals(p.getDisponible()) ? "Si" : "No",
                    p.getCategoria() != null ? p.getCategoria().getNombre() : "N/A");
        }
    }

    private Double parseDouble(String s) {
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return null; }
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }
}
