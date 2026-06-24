package com.tp.jpa;

import com.tp.jpa.menu.*;
import com.tp.jpa.repository.*;
import com.tp.jpa.util.JPAUtil;

import java.util.Scanner;

public class Main {

    static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("     Food Store — Sistema de Gestión         ");
        System.out.println("==============================================");

        CategoriaRepository categoriaRepo = new CategoriaRepository();
        ProductoRepository  productoRepo  = new ProductoRepository();
        UsuarioRepository   usuarioRepo   = new UsuarioRepository();
        PedidoRepository    pedidoRepo    = new PedidoRepository();

        CategoriaMenu categoriaMenu = new CategoriaMenu(categoriaRepo, scanner);
        ProductoMenu  productoMenu  = new ProductoMenu(productoRepo, categoriaRepo, scanner);
        UsuarioMenu   usuarioMenu   = new UsuarioMenu(usuarioRepo, scanner);
        PedidoMenu    pedidoMenu    = new PedidoMenu(pedidoRepo, productoRepo, usuarioRepo, scanner);
        ReporteMenu   reporteMenu   = new ReporteMenu(categoriaRepo, pedidoRepo, usuarioRepo, scanner);

        boolean salir = false;
        while (!salir) {
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("1. Gestionar Categorías");
            System.out.println("2. Gestionar Productos");
            System.out.println("3. Gestionar Usuarios");
            System.out.println("4. Gestionar Pedidos");
            System.out.println("5. Reportes");
            System.out.println("0. Salir");
            System.out.print("Seleccioná una opción: ");
            String opcion = scanner.nextLine().trim();
            switch (opcion) {
                case "1" -> categoriaMenu.mostrar();
                case "2" -> productoMenu.mostrar();
                case "3" -> usuarioMenu.mostrar();
                case "4" -> pedidoMenu.mostrar();
                case "5" -> reporteMenu.mostrar();
                case "0" -> salir = true;
                default  -> System.out.println("Opción inválida. Ingresá un número del 0 al 5.");
            }
        }

        JPAUtil.close();
        System.out.println("Aplicación finalizada. ¡Hasta luego!");
    }
}
