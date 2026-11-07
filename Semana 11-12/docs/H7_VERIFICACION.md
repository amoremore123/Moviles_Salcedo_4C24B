# H7 — Verificación de detalle remoto y navegación

## Resultado técnico

`:app:assembleDebug :app:testDebugUnitTest` terminó con **BUILD SUCCESSFUL**. Los informes XML registran **40 pruebas aprobadas, 0 fallos, 0 errores y 0 omitidas**: las 28 anteriores y 12 nuevas, sin Internet en las pruebas unitarias.

Se añadió Navigation Compose 2.7.7. Se conservan Kotlin, Gradle, AGP, Retrofit y Coil 2.7.0. El permiso de Internet ya estaba configurado.

## Endpoint y separación del catálogo

`ItemRepositoryRemoto.obtenerDetalle(id)` llama a `ApiService.obtenerDetalle(id)` (`GET products/{id}`) en Dispatchers.IO. Valida el ID, convierte el DTO con el mapper existente y devuelve Result. Una respuesta inválida, un ID diferente, HTTP 404 u otro fallo HTTP/de conexión se informan como fallos controlados. La cancelación se propaga. No se modifica la lista publicada ni se devuelve su objeto como sustituto de una consulta fallida.

Las consultas HTTP reales desde el equipo a `https://dummyjson.com/products/121` y `https://dummyjson.com/products/122` devolvieron HTTP 200 e identificaron iPhone 5s (199.99) e iPhone 6 (299.99). El registro con fecha se conserva en `.h1-verification/h7/http-detalles.json`.

## Comprobaciones reales en el emulador

| Caso | Resultado observado |
|---|---|
| Detalles con Internet | IDs 121, 122 y 124: nombres, precios, descripción completa e imágenes reales |
| Botón superior | Regresa al catálogo |
| Atrás del dispositivo | Regresa al catálogo |
| Dos pulsaciones rápidas | Un solo detalle; un Atrás devuelve directamente al catálogo |
| Conservación de posición | Los límites de la fila iPhone X coinciden antes de entrar y después de volver |
| Detalle sin Internet | Error con Reintentar y Volver, aun cuando el producto está presente en la lista en memoria |
| Regreso sin Internet | La lista y el desplazamiento permanecen disponibles sin una nueva consulta del catálogo |
| Recuperación | Restaurar la conexión y pulsar Reintentar recupera el detalle real del ID 124 sin reiniciar la app |

No se observaron errores AndroidRuntime ni cierres inesperados de SmartCatalog. Se alteró únicamente la red del emulador y se restauraron sus valores originales de Wi-Fi, datos y modo avión. No se modificó la conectividad del equipo anfitrión.

## Evidencias principales reales (excluidas de Git)

- `evidencias/h7/01-detalle-iphone-5s.png`
- `evidencias/h7/02-detalle-iphone-6.png`
- `evidencias/h7/03-error-conexion.png`
- `evidencias/h7/04-recuperacion.png`

La carpeta incluye también capturas de las comprobaciones de regreso, pulsaciones rápidas y desplazamiento. Las jerarquías XML y los registros locales de verificación están en `.h1-verification/h7/`, excluida de Git.

## Navegación y ciclo de vida

`AppNav` contiene `lista` y `detalle/{id}`, con argumento Int. Las tarjetas envían únicamente el ID. La navegación comprueba el destino y el estado RESUMED y utiliza launchSingleTop para evitar entradas duplicadas. Cada detalle obtiene su ViewModel de su NavBackStackEntry mediante una fábrica de AppContainer. DetalleViewModel solo depende de ItemRepository y consulta en su inicialización, no por recomposición. El ViewModel de la lista permanece ligado a la actividad y el estado de scroll se conserva mediante Navigation Compose.

DetalleScreen reutiliza Cargando y ErrorView. El regreso permanece visible en carga, éxito y error. Coil muestra progreso durante la carga de la imagen, una alternativa al fallar y ContentScale.Fit, con caché en disco desactivada. Se conserva el criterio neutral de precios de H6, sin atribuir soles ni inventar una conversión.

Los IDs inválidos, los errores HTTP, la respuesta con ID incorrecto y la cancelación se comprobaron con pruebas unitarias y objetos falsos. No se añadieron Room, persistencia, modo offline real, Firebase, login, favoritos persistentes, administración ni notificaciones.
