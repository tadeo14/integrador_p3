package com.tp.jpa.menu;

import com.tp.jpa.model.Usuario;
import com.tp.jpa.model.enums.Rol;
import com.tp.jpa.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UsuarioMenu {

    private final UsuarioRepository repo;
    private final Scanner scanner;

    public UsuarioMenu(UsuarioRepository repo, Scanner scanner) {
        this.repo = repo;
        this.scanner = scanner;
    }

    public void mostrar() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- GESTION DE USUARIOS ---");
            System.out.println("1. Alta");
            System.out.println("2. Modificar");
            System.out.println("3. Baja logica");
            System.out.println("4. Listado");
            System.out.println("5. Buscar por mail");
            System.out.println("0. Volver");
            System.out.print("Selecciona una opcion: ");
            String op = scanner.nextLine().trim();
            switch (op) {
                case "1" -> alta();
                case "2" -> modificar();
                case "3" -> bajaLogica();
                case "4" -> listado();
                case "5" -> buscarPorMail();
                case "0" -> volver = true;
                default  -> System.out.println("Opcion invalida.");
            }
        }
    }

    private void alta() {
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine().trim();
        System.out.print("Apellido: ");
        String apellido = scanner.nextLine().trim();
        System.out.print("Mail: ");
        String mail = scanner.nextLine().trim();

        if (repo.buscarPorMail(mail).isPresent()) {
            System.out.println("Error: ya existe un usuario activo con ese mail.");
            return;
        }

        System.out.print("Celular (opcional): ");
        String celular = scanner.nextLine().trim();
        System.out.print("Contrasena: ");
        String contrasena = scanner.nextLine().trim();

        System.out.println("Rol: 1. ADMIN  2. USUARIO");
        System.out.print("Selecciona: ");
        String rolStr = scanner.nextLine().trim();
        Rol rol = switch (rolStr) {
            case "1" -> Rol.ADMIN;
            case "2" -> Rol.USUARIO;
            default  -> null;
        };
        if (rol == null) { System.out.println("Rol invalido."); return; }

        Usuario usuario = Usuario.builder()
                .nombre(nombre)
                .apellido(apellido)
                .mail(mail)
                .celular(celular.isEmpty() ? null : celular)
                .contrasena(contrasena)
                .rol(rol)
                .build();
        Usuario guardado = repo.guardar(usuario);
        System.out.println("Usuario creado con ID: " + guardado.getId());
    }

    private void modificar() {
        List<Usuario> activos = repo.listarActivos();
        if (activos.isEmpty()) { System.out.println("No hay usuarios activos."); return; }
        imprimirListado(activos);

        System.out.print("ID a modificar: ");
        Long id = CategoriaMenu.parseLong(scanner.nextLine().trim());
        if (id == null) { System.out.println("ID invalido."); return; }

        Optional<Usuario> opt = repo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("Usuario no encontrado o dado de baja.");
            return;
        }
        Usuario u = opt.get();

        System.out.println("Nombre actual: " + u.getNombre());
        System.out.print("Nuevo nombre (Enter para conservar): ");
        String nombre = scanner.nextLine().trim();
        if (!nombre.isEmpty()) u.setNombre(nombre);

        System.out.println("Apellido actual: " + u.getApellido());
        System.out.print("Nuevo apellido (Enter para conservar): ");
        String apellido = scanner.nextLine().trim();
        if (!apellido.isEmpty()) u.setApellido(apellido);

        System.out.println("Mail actual: " + u.getMail());
        System.out.print("Nuevo mail (Enter para conservar): ");
        String mail = scanner.nextLine().trim();
        if (!mail.isEmpty()) {
            Optional<Usuario> existente = repo.buscarPorMail(mail);
            if (existente.isPresent() && !existente.get().getId().equals(id)) {
                System.out.println("Error: ese mail ya esta en uso por otro usuario.");
            } else {
                u.setMail(mail);
            }
        }

        System.out.println("Celular actual: " + u.getCelular());
        System.out.print("Nuevo celular (Enter para conservar): ");
        String celular = scanner.nextLine().trim();
        if (!celular.isEmpty()) u.setCelular(celular);

        System.out.print("Nueva contrasena (Enter para conservar): ");
        String contrasena = scanner.nextLine().trim();
        if (!contrasena.isEmpty()) u.setContrasena(contrasena);

        repo.guardar(u);
        System.out.println("Usuario actualizado correctamente.");
    }

    private void bajaLogica() {
        System.out.print("ID del usuario a dar de baja: ");
        Long id = CategoriaMenu.parseLong(scanner.nextLine().trim());
        if (id == null) { System.out.println("ID invalido."); return; }

        Optional<Usuario> opt = repo.buscarPorId(id);
        String nombreCompleto = opt.map(u -> u.getNombre() + " " + u.getApellido()).orElse("(desconocido)");

        if (repo.eliminarLogico(id)) {
            System.out.println("Usuario \"" + nombreCompleto + "\" dado de baja. Sus pedidos permanecen en el sistema.");
        } else {
            System.out.println("Error: no se encontro el usuario o ya estaba dado de baja.");
        }
    }

    private void listado() {
        List<Usuario> activos = repo.listarActivos();
        if (activos.isEmpty()) { System.out.println("No hay usuarios activos."); return; }
        System.out.println("\n--- USUARIOS ACTIVOS ---");
        imprimirListado(activos);
    }

    private void buscarPorMail() {
        System.out.print("Mail a buscar: ");
        String mail = scanner.nextLine().trim();
        Optional<Usuario> opt = repo.buscarPorMail(mail);
        if (opt.isEmpty()) {
            System.out.println("No existe usuario activo con el mail: " + mail);
        } else {
            Usuario u = opt.get();
            System.out.println("\n--- USUARIO ENCONTRADO ---");
            System.out.println("ID:       " + u.getId());
            System.out.println("Nombre:   " + u.getNombre() + " " + u.getApellido());
            System.out.println("Mail:     " + u.getMail());
            System.out.println("Celular:  " + (u.getCelular() != null ? u.getCelular() : "N/A"));
            System.out.println("Rol:      " + u.getRol());
        }
    }

    public void imprimirListado(List<Usuario> lista) {
        System.out.printf("%-5s %-20s %-30s %s%n", "ID", "Nombre completo", "Mail", "Rol");
        System.out.println("-".repeat(70));
        for (Usuario u : lista) {
            System.out.printf("%-5d %-20s %-30s %s%n",
                    u.getId(), u.getNombre() + " " + u.getApellido(), u.getMail(), u.getRol());
        }
    }
}
