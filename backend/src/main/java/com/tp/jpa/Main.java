package com.tp.jpa;

import com.tp.jpa.util.JPAUtil;

import java.util.Scanner;

public class Main {

    static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Food Store - Sistema de Gestión de Pedidos ===");

        boolean salir = false;
        while (!salir) {
            mostrarMenuPrincipal();
            String opcion = scanner.nextLine().trim();
            switch (opcion) {
                case "1" -> System.out.println("[Categorías - próximamente]");
                case "2" -> System.out.println("[Productos - próximamente]");
                case "3" -> System.out.println("[Usuarios - próximamente]");
                case "4" -> System.out.println("[Pedidos - próximamente]");
                case "5" -> System.out.println("[Reportes - próximamente]");
                case "0" -> salir = true;
                default  -> System.out.println("Opción inválida. Ingresá un número del 0 al 5.");
            }
        }

        JPAUtil.close();
        System.out.println("Aplicación finalizada. ¡Hasta luego!");
    }

    private static void mostrarMenuPrincipal() {
        System.out.println("\n--- MENÚ PRINCIPAL ---");
        System.out.println("1. Gestionar Categorías");
        System.out.println("2. Gestionar Productos");
        System.out.println("3. Gestionar Usuarios");
        System.out.println("4. Gestionar Pedidos");
        System.out.println("5. Reportes");
        System.out.println("0. Salir");
        System.out.print("Seleccioná una opción: ");
    }
}
