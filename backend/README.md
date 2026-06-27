# Food Store — Backend

Sistema de gestión de pedidos de comida implementado en Java con JPA, Hibernate y base de datos H2 en archivo. La interacción se realiza a través de un menú de consola navegable.

## Tecnologías

- Java 17
- Gradle
- JPA / Hibernate 6.4
- H2 (modo archivo: `./data/jpa_db`)
- Lombok

## Estructura de paquetes

```
com.tp.jpa/
├── model/          # Entidades JPA: Base, Categoria, Producto, Usuario, Pedido, DetallePedido
│   └── enums/      # Rol, Estado, FormaPago
├── repository/     # BaseRepository<T> y repos específicos
├── menu/           # Submenús de consola por entidad
├── util/           # JPAUtil (singleton EntityManagerFactory)
└── Main.java       # Punto de entrada con menú principal
```

## Instalación y ejecución

### Requisitos

- Java 17 o superior
- **No se requiere Gradle instalado** — el proyecto incluye el Gradle Wrapper (`gradlew`)

### Ejecutar en Windows (PowerShell o CMD)

```powershell
cd backend
.\gradlew.bat run
```

### Ejecutar en Linux / macOS

```bash
cd backend
./gradlew run
```

La primera vez, el wrapper descarga automáticamente Gradle. La base de datos se crea en `./data/jpa_db.mv.db` al primer arranque (`hbm2ddl.auto = update`).

### Otros comandos útiles

```powershell
# Solo compilar
.\gradlew.bat compileJava

# Generar JAR ejecutable
.\gradlew.bat jar
java -jar build/libs/food-store-backend-1.0.jar
```

## Orden de uso recomendado

1. **Categorías** → crear al menos una antes de agregar productos
2. **Productos** → asociar a una categoría existente
3. **Usuarios** → crear al menos uno antes de generar pedidos
4. **Pedidos** → seleccionar usuario, forma de pago y productos

## Notas técnicas

- Las bajas son siempre **lógicas** (`eliminado = true`). Ningún registro se elimina físicamente.
- El alta de pedido se ejecuta en una **única transacción atómica**: si falla cualquier validación, se hace rollback completo y el stock no se modifica.
- Dejar un campo en blanco durante una modificación **conserva el valor anterior**.
- Al salir (opción 0), se cierra correctamente el `EntityManagerFactory`.
