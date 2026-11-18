# M2 — Rediseño visual

La bienvenida ofrece acceso al login y al formulario de registro preparado. Una sesión recuperada abre directamente el catálogo o el panel según su rol. Se conservan la sincronización, los avisos offline, los favoritos en Room y los permisos por rol.

## Tema fucsia

La interfaz usa una paleta fucsia (`#E91E8C`) con degradado a morado (`#9C27B0`), fondo rosa claro y tarjetas blancas con sombra suave. El corazón de favoritos usa un rojo rosado. El modo oscuro aplica la misma identidad sobre un fondo vino oscuro. Los colores, las formas redondeadas y la tipografía en negrita están en `Theme.kt`. El degradado se expone como `ColoresMarca.degradado`.

`Marca.kt` reúne las piezas compartidas:

- `CabeceraMarca`: cabecera con degradado, esquinas inferiores redondeadas y contenido en blanco.
- `FondoOndas`: fondo con ondas suaves para las pantallas de acceso.
- `BotonPrincipal`: botón en forma de píldora con el degradado.
- `coloresCampo`: campos de texto blancos con borde rosado.

Tres ilustraciones vectoriales de un gatito acompañan las pantallas: con laptop en el acceso y la carga, dormido con el wifi en los errores, y guiñando con un corazón cuando no hay elementos.

## Pantallas

- **Acceso.** Bienvenida, login y registro con fondo de ondas, tarjeta blanca, campos redondeados con ícono y botón con degradado. El login invita a registrarse con «¿Eres nuevo? Regístrate aquí».
- **Catálogo.** Cuadrícula adaptable de tarjetas con imagen, corazón en la esquina, nombre y precio. El buscador, los filtros y el orden usan forma de píldora.
- **Detalle.** Imagen grande, categoría como etiqueta, precio destacado y botón para agregar o quitar de favoritos.
- **Favoritos.** Filas con miniatura, nombre, precio y corazón, bajo un aviso de que se guardan en el dispositivo.
- **Perfil.** Avatar con degradado, nombre, correo, etiqueta de rol y selector de tema con la opción elegida resaltada.
- **Panel de administrador.** Bienvenida, contadores con ícono y acciones rápidas para sincronizar y limpiar la caché.
- **Estados.** Carga, error con «Reintentar» y estado vacío, cada uno con su gatito.
- **Navegación.** Barra inferior blanca y redondeada con la pestaña activa en fucsia.

## Evidencias

En `evidencias/mejoras/m2/` se guardan las pantallas de bienvenida, login, registro, catálogo, detalle, favoritos, panel y catálogo offline con el tema fucsia. Las capturas corresponden al emulador y los flujos autenticados usan proveedores de prueba. El modo offline se simula con un error de conexión de la API y productos ya guardados en Room.

La versión con el tema fucsia terminó con BUILD SUCCESSFUL. Pasaron 140 pruebas unitarias y las 50 instrumentadas de la suite en el emulador. También se revisaron en modo oscuro el catálogo, el aviso offline y el perfil.
