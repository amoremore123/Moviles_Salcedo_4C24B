# Registro de Prompts - Laboratorio 06 (Semana 06)

Este documento registra los prompts utilizados durante la Fase 2 (Mejora con IA) del Laboratorio 06 para integrar el contador de favoritos (badge) en el `NavigationDrawer` conectado con el `DropdownMenu` de las tarjetas de productos.

---

### Prompt 1: Integración del contador de favoritos en el Drawer
**Prompt utilizado:**
> *"Actúa como un desarrollador experto en Jetpack Compose. Necesito conectar el estado de favoritos de cada producto (manejado en las tarjetas con DropdownMenu) con el NavigationDrawer de la aplicación TECSUP Store. Específicamente, necesito que el ítem 'Favoritos' del Drawer muestre un Badge con el contador dinámico de cuántos productos ha marcado el usuario como favoritos."*

**Resultado / Implementación generada:**
- Creación de la clase de datos `Product` con la propiedad `isFavorite: Boolean`.
- Implementación de `AppDrawer` recibiendo `favoritesCount: Int` y mostrando un componente `Badge` con el número total de productos favoritos cuando `favoritesCount > 0`.
- Conexión del estado `products` en `AppNavegacion` para actualizar el contador en tiempo real al hacer clic en "Favoritos" desde el menú contextual de cualquier tarjeta de producto.

---

### Prompt 2: Estructura y Estilos Material 3 para TarjetaProducto y Drawer
**Prompt utilizado:**
> *"Diseña los componentes `TarjetaProducto.kt` con un menú contextual (`DropdownMenu`) de 3 puntos que incluya opciones con íconos para 'Favoritos', 'Compartir' y 'Reportar', así como el diseño del encabezado del `NavigationDrawer` con las iniciales y datos del usuario (Maria Rojas)."*

**Resultado / Implementación generada:**
- Componente `TarjetaProducto` utilizando `Card`, `Box`, `IconButton` y `DropdownMenu` con separadores (`HorizontalDivider`) e íconos (`leadingIcon`).
- Encabezado de `ModalDrawerSheet` con un avatar circular con iniciales ("MR"), nombre y correo electrónico corporativo.
- Resaltado visual del ítem activo en el menú lateral.
