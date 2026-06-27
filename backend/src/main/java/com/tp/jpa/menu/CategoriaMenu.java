package com.tp.jpa.menu;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.repository.CategoriaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class CategoriaMenu {

    private final CategoriaRepository repo;
    private final Scanner scanner;

    public CategoriaMenu(CategoriaRepository repo, Scanner scanner) {
        this.repo = repo;
        this.scanner = scanner;
    }

    public void mostrar() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- GESTION DE CATEGORIAS ---");
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
        System.out.print("Nombre (obligatorio): ");
        String nombre = scanner.nextLine().trim();
        if (nombre.isEmpty()) {
            System.out.println("Error: el nombre no puede estar vacio.");
            return;
        }
        System.out.print("Descripcion (opcional, Enter para omitir): ");
        String descripcion = scanner.nextLine().trim();

        Categoria cat = Categoria.builder()
                .nombre(nombre)
                .descripcion(descripcion.isEmpty() ? null : descripcion)
                .build();
        Categoria guardada = repo.guardar(cat);
        System.out.println("Categoria creada con ID: " + guardada.getId());
    }

    private void modificar() {
        List<Categoria> activas = repo.listarActivos();
        if (activas.isEmpty()) {
            System.out.println("No hay categorias activas.");
            return;
        }
        imprimirListado(activas);
        System.out.print("ID a modificar: ");
        Long id = parseLong(scanner.nextLine().trim());
        if (id == null) { System.out.println("ID invalido."); return; }

        Optional<Categoria> opt = repo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("Error: no existe categoria activa con ese ID.");
            return;
        }
        Categoria cat = opt.get();

        System.out.println("Nombre actual: " + cat.getNombre());
        System.out.print("Nuevo nombre (Enter para conservar): ");
        String nombre = scanner.nextLine().trim();
        if (!nombre.isEmpty()) cat.setNombre(nombre);

        System.out.println("Descripcion actual: " + cat.getDescripcion());
        System.out.print("Nueva descripcion (Enter para conservar): ");
        String desc = scanner.nextLine().trim();
        if (!desc.isEmpty()) cat.setDescripcion(desc);

        repo.guardar(cat);
        System.out.println("Categoria actualizada correctamente.");
    }

    private void bajaLogica() {
        System.out.print("ID de la categoria a dar de baja: ");
        Long id = parseLong(scanner.nextLine().trim());
        if (id == null) { System.out.println("ID invalido."); return; }

        Optional<Categoria> opt = repo.buscarPorId(id);
        String nombre = opt.map(Categoria::getNombre).orElse("(desconocida)");

        if (repo.eliminarLogico(id)) {
            System.out.println("Categoria \"" + nombre + "\" dada de baja exitosamente.");
        } else {
            System.out.println("Error: no se encontro la categoria o ya estaba dada de baja.");
        }
    }

    private void listado() {
        List<Categoria> activas = repo.listarActivos();
        if (activas.isEmpty()) {
            System.out.println("No hay categorias activas.");
            return;
        }
        System.out.println("\n--- CATEGORIAS ACTIVAS ---");
        imprimirListado(activas);
    }

    public void imprimirListado(List<Categoria> lista) {
        System.out.printf("%-5s %-25s %s%n", "ID", "Nombre", "Descripcion");
        System.out.println("-".repeat(65));
        for (Categoria c : lista) {
            System.out.printf("%-5d %-25s %s%n",
                    c.getId(), c.getNombre(),
                    c.getDescripcion() != null ? c.getDescripcion() : "");
        }
    }

    static Long parseLong(String s) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }
}
