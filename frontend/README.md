# Food Store — Frontend

Frontend web del sistema de gestión de pedidos Food Store.
Tecnologías: TypeScript, Vite, HTML5, CSS3.

## Instalación y ejecución

```bash
npm install
npm run dev
```

Luego abrí http://localhost:5173 en el navegador.

## Credenciales de prueba

| Rol    | Email                  | Contraseña  |
|--------|------------------------|-------------|
| ADMIN  | admin@foodstore.com    | admin123    |
| USUARIO| juan@mail.com          | usuario123  |
| USUARIO| maria@mail.com         | pass456     |

## Costo de envío

El costo de envío está definido como constante `ENVIO = 0` en `src/utils/cart.ts`.
Para modificarlo, cambiá ese valor antes de hacer `npm run build`.

## Estructura de fetch

Todos los fetch apuntan a `/data/*.json` (carpeta `public/data/`).
Para conectar la API REST, reemplazá las URLs en `src/utils/api.ts`:

```ts
// Antes (iteración 1):
fetch('/data/productos.json')

// Después (iteración 2):
fetch('/api/products')
```
