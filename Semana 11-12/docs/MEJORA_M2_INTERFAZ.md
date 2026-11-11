# M2 — Rediseño visual

La bienvenida ofrece acceso al login y al formulario de registro preparado. Una sesión recuperada abre directamente el catálogo o el panel según su rol. El login y el registro comparten encabezado, ancho máximo, desplazamiento con teclado y estilos Material 3.

El catálogo presenta tarjetas compactas con imagen, nombre, precio y favorito. Detalle y favoritos comparten el tratamiento de imágenes locales y remotas. El panel reorganiza el encabezado, las estadísticas y las acciones de mantenimiento. Se conservan la sincronización, los avisos offline, los favoritos en Room y los permisos por rol.

La paleta, las formas y la jerarquía tipográfica están en el tema. Las imágenes de bienvenida y de las pruebas se cargan desde recursos empaquetados.

## Evidencias

En `evidencias/mejoras/m2/` se guardan las pantallas de bienvenida, login, registro, catálogo, detalle, favoritos, panel y catálogo offline. Las capturas corresponden al emulador y los flujos autenticados usan proveedores de prueba. El modo offline se simula con un error de conexión de la API y productos ya guardados en Room.

Gradle terminó con BUILD SUCCESSFUL. Pasaron 133 pruebas unitarias y 14 instrumentadas de navegación, registro, panel y recreación de actividad. Se repitió con éxito el flujo de bienvenida y registro en un tamaño de 840 × 1600 píxeles en el emulador.
